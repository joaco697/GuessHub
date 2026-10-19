package com.guesshub.service;

import com.guesshub.dto.MiPosicionDTO;
import com.guesshub.dto.RankingDTO;
import com.guesshub.entity.Puntaje;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.PuntajeRepository;
import com.guesshub.repository.security.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PuntajeService {

    private final PuntajeRepository puntajeRepository;
    private final UsuarioRepository usuarioRepository;

    /** Multiplicador máximo permitido (x2 según relevamiento) */
    private static final double MULTIPLICADOR_MAX = 2.0;

    /**
     * Calcula el puntaje base de una partida.
     * - Base: 1000 pts
     * - Pista 2 usada: -200
     * - Pista 3 usada: -200
     * - Tiempo: -5 pts por segundo
     * - Racha: +0.05 multiplicador por cada día de racha (máx x2)
     */
    public int calcularPuntos(boolean correcto, int pistasUsadas, int segundosUsados, int rachaDias) {
        if (!correcto) return 0;

        int puntos = 1000;
        if (pistasUsadas >= 2) puntos -= 200;
        if (pistasUsadas >= 3) puntos -= 200;
        puntos -= (segundosUsados * 5);
        puntos = Math.max(0, puntos);

        // Multiplicador por racha: +0.05 por cada día (ej: 3 días → x1.15, 7 días → x1.35)
        double multiplicador = 1.0 + (rachaDias * 0.05);
        multiplicador = Math.min(multiplicador, MULTIPLICADOR_MAX);

        return (int) Math.round(puntos * multiplicador);
    }

    @Transactional
    public int guardarPartida(boolean correcto, int pistasUsadas, int segundosUsados, String categoria) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Puntaje puntaje = puntajeRepository.findByUsuarioId(usuario.getId())
                .orElse(new Puntaje(usuario));

        // ── Actualizar racha ──────────────────────────────────────────────
        LocalDate hoy = LocalDate.now();
        LocalDate ultima = puntaje.getUltimaPartidaFecha();

        if (ultima == null) {
            // Primera partida
            puntaje.setRachaDias(1);
        } else if (ultima.equals(hoy)) {
            // Ya jugó hoy, la racha no cambia
        } else if (ultima.equals(hoy.minusDays(1))) {
            // Jugó ayer → racha continúa
            puntaje.setRachaDias(puntaje.getRachaDias() + 1);
        } else {
            // Pasó más de un día → racha se reinicia
            puntaje.setRachaDias(1);
        }
        puntaje.setUltimaPartidaFecha(hoy);

        // ── Calcular puntaje con multiplicador de racha ───────────────────
        int puntosGanados = calcularPuntos(correcto, pistasUsadas, segundosUsados, puntaje.getRachaDias());

        // ── Actualizar totales ────────────────────────────────────────────
        puntaje.setPuntos(puntaje.getPuntos() + puntosGanados);
        puntaje.setPartidasJugadas(puntaje.getPartidasJugadas() + 1);
        if (correcto) puntaje.setPartidasCorrectas(puntaje.getPartidasCorrectas() + 1);

        // ── Actualizar puntaje por categoría ─────────────────────────────
        if (categoria != null) {
            switch (categoria.toLowerCase()) {
                case "pelicula", "peliculas" ->
                        puntaje.setPuntosPeliculas(puntaje.getPuntosPeliculas() + puntosGanados);
                case "cancion", "musica" ->
                        puntaje.setPuntosMusica(puntaje.getPuntosMusica() + puntosGanados);
                case "equipo", "deportes" ->
                        puntaje.setPuntosDeportes(puntaje.getPuntosDeportes() + puntosGanados);
            }
        }

        puntajeRepository.save(puntaje);
        log.info("Partida guardada para {}: +{} pts (racha {}d, cat: {})",
                username, puntosGanados, puntaje.getRachaDias(), categoria);

        return puntosGanados;
    }

    /**
     * Posición real del usuario autenticado, aunque esté fuera del Top 10.
     */
    public MiPosicionDTO getMiPosicion() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return puntajeRepository.findByUsuarioId(usuario.getId())
                .map(p -> new MiPosicionDTO(
                        puntajeRepository.findPosicion(usuario.getId()),
                        p.getPuntos(),
                        p.getPartidasJugadas(),
                        p.getPartidasCorrectas(),
                        p.getRachaDias(),
                        p.getPuntosPeliculas(),
                        p.getPuntosMusica(),
                        p.getPuntosDeportes()))
                .orElse(new MiPosicionDTO(0, 0, 0, 0, 0, 0, 0, 0));
    }

    /**
     * Convierte una fila de la query nativa a RankingDTO.
     * Columnas: [0]=id [1]=usuario_id [2]=puntos [3]=partidas_jugadas [4]=partidas_correctas [5]=display_name [6]=racha_dias
     */
    private RankingDTO toRankingDTO(int posicion, Object[] row) {
        return new RankingDTO(
                posicion,
                (String) row[5],
                ((Number) row[2]).intValue(),
                ((Number) row[3]).intValue(),
                ((Number) row[4]).intValue(),
                ((Number) row[6]).intValue()
        );
    }

    public List<RankingDTO> getRanking() {
        return buildRanking(puntajeRepository.findTop10());
    }

    public List<RankingDTO> getRankingPeliculas() {
        return buildRanking(puntajeRepository.findTop10Peliculas());
    }

    public List<RankingDTO> getRankingMusica() {
        return buildRanking(puntajeRepository.findTop10Musica());
    }

    public List<RankingDTO> getRankingDeportes() {
        return buildRanking(puntajeRepository.findTop10Deportes());
    }

    private List<RankingDTO> buildRanking(List<Object[]> rows) {
        List<RankingDTO> ranking = new ArrayList<>();
        int currentRank = 1;
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            int puntos = ((Number) row[2]).intValue();
            if (i > 0) {
                int prevPuntos = ((Number) rows.get(i - 1)[2]).intValue();
                if (puntos < prevPuntos) {
                    currentRank = i + 1;
                }
            }
            ranking.add(toRankingDTO(currentRank, row));
        }
        return ranking;
    }
}