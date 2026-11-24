package com.guesshub.service;

import com.guesshub.dto.MiPosicionDTO;
import com.guesshub.dto.PartidaDTO;
import com.guesshub.dto.RankingDTO;
import com.guesshub.dto.ResultadoPartidaDTO;
import com.guesshub.entity.Partida;
import com.guesshub.entity.Puntaje;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.PartidaRepository;
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
    private final PartidaRepository partidaRepository;
    private final InsigniaService insigniaService;

    /** Multiplicador máximo permitido (x2 según relevamiento) */
    public static final double MULTIPLICADOR_MAX = 2.0;

    /**
     * Calcula el multiplicador combinado de una partida respetando las 3 fuentes del relevamiento:
     * 1. Dificultad: Fácil (1.0x), Medio (1.2x), Difícil (1.4x)
     * 2. Racha global: +0.05 por día consecutivo
     * 3. Racha en partida: +0.05 por cada respuesta correcta consecutiva a partir de la segunda
     * Cap máximo total: 2.0x
     */
    public double calcularMultiplicador(String dificultad, int rachaDias, int rachaPartida) {
        double multDificultad = switch (dificultad != null ? dificultad.toUpperCase() : "FACIL") {
            case "MEDIO" -> 1.2;
            case "DIFICIL" -> 1.4;
            default -> 1.0;
        };

        double bonusRachaDias = rachaDias > 0 ? (rachaDias * 0.05) : 0.0;
        double bonusRachaPartida = rachaPartida >= 2 ? ((rachaPartida - 1) * 0.05) : 0.0;

        double total = multDificultad + bonusRachaDias + bonusRachaPartida;
        return Math.min(MULTIPLICADOR_MAX, Math.round(total * 100.0) / 100.0);
    }

    /**
     * Calcula el puntaje de una partida:
     * - Base: 1000 pts
     * - Pista 2 usada: -200
     * - Pista 3 usada: -200
     * - Tiempo: -5 pts por segundo
     * - Si es correcta, siempre suma puntos (mínimo 50 pts antes de multiplicador)
     */
    public int calcularPuntos(boolean correcto, int pistasUsadas, int segundosUsados, double multiplicador) {
        if (!correcto) return 0;

        int puntosBase = 1000;
        if (pistasUsadas >= 2) puntosBase -= 200;
        if (pistasUsadas >= 3) puntosBase -= 200;
        puntosBase -= (segundosUsados * 5);

        // "Respuesta correcta siempre otorga puntos"
        puntosBase = Math.max(50, puntosBase);

        return (int) Math.round(puntosBase * multiplicador);
    }

    @Transactional
    public ResultadoPartidaDTO registrarPartida(PartidaDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Puntaje puntaje = puntajeRepository.findByUsuarioId(usuario.getId())
                .orElse(new Puntaje(usuario));

        LocalDate hoy = LocalDate.now();
        LocalDate ultima = puntaje.getUltimaPartidaFecha();

        // ── 1. Actualizar Racha Global (con soporte para Racha Congelada) ────
        if (ultima == null) {
            puntaje.setRachaDias(1);
        } else if (ultima.equals(hoy)) {
            // Ya jugó hoy, mantiene racha
        } else if (ultima.equals(hoy.minusDays(1))) {
            // Jugó ayer → racha continúa
            puntaje.setRachaDias(puntaje.getRachaDias() + 1);
        } else {
            // Pasó más de un día
            if (puntaje.isRachaCongelada()) {
                // Comodín de racha consumido: no se reinicia
                puntaje.setRachaCongelada(false);
                puntaje.setRachaDias(puntaje.getRachaDias() + 1);
                log.info("Racha congelada protegió al usuario {}", username);
            } else {
                puntaje.setRachaDias(1);
            }
        }
        puntaje.setUltimaPartidaFecha(hoy);

        // ── 2. Racha por Partida (respuestas consecutivas) ──────────────────
        if (dto.correcto()) {
            puntaje.setRachaPartidaActual(puntaje.getRachaPartidaActual() + 1);
            if (puntaje.getRachaPartidaActual() > puntaje.getRachaPartidaMaxima()) {
                puntaje.setRachaPartidaMaxima(puntaje.getRachaPartidaActual());
            }
        } else {
            puntaje.setRachaPartidaActual(0);
        }

        int rachaPartidaEfectiva = dto.rachaPartida() != null && dto.rachaPartida() > 0
                ? dto.rachaPartida()
                : puntaje.getRachaPartidaActual();

        // ── 3. Calcular Multiplicadores y Puntos ────────────────────────────
        String dif = dto.dificultad() != null ? dto.dificultad() : "FACIL";
        double multiplicador = calcularMultiplicador(dif, puntaje.getRachaDias(), rachaPartidaEfectiva);
        int puntosGanados = calcularPuntos(dto.correcto(), dto.pistasUsadas(), dto.segundosUsados(), multiplicador);

        // ── 4. Actualizar Totales del Usuario ────────────────────────────────
        puntaje.setPuntos(puntaje.getPuntos() + puntosGanados);
        puntaje.setPartidasJugadas(puntaje.getPartidasJugadas() + 1);
        if (dto.correcto()) puntaje.setPartidasCorrectas(puntaje.getPartidasCorrectas() + 1);

        if (dto.categoria() != null) {
            switch (dto.categoria().toLowerCase()) {
                case "pelicula", "peliculas" ->
                        puntaje.setPuntosPeliculas(puntaje.getPuntosPeliculas() + puntosGanados);
                case "cancion", "musica" ->
                        puntaje.setPuntosMusica(puntaje.getPuntosMusica() + puntosGanados);
                case "equipo", "deportes" ->
                        puntaje.setPuntosDeportes(puntaje.getPuntosDeportes() + puntosGanados);
            }
        }

        puntajeRepository.save(puntaje);

        // ── 5. Persistir la Partida individual (para Ranking Diario) ─────────
        boolean esDiario = Boolean.TRUE.equals(dto.esDesafioDiario());
        Partida partida = new Partida(
                usuario,
                dto.categoria() != null ? dto.categoria() : "random",
                puntosGanados,
                dto.correcto(),
                dto.pistasUsadas(),
                dto.segundosUsados(),
                dif,
                esDiario,
                multiplicador
        );
        partidaRepository.save(partida);

        // ── 6. Evaluar Logros / Insignias Desbloqueadas ─────────────────────
        int posRanking = puntajeRepository.findPosicion(usuario.getId());
        List<String> insigniasNuevas = insigniaService.evaluarInsignias(usuario, puntaje, puntaje.getRachaPartidaActual(), posRanking);

        log.info("Partida guardada para {}: +{} pts (mult: {}x, racha global: {}d, racha partida: {})",
                username, puntosGanados, multiplicador, puntaje.getRachaDias(), puntaje.getRachaPartidaActual());

        return new ResultadoPartidaDTO(
                puntosGanados,
                multiplicador,
                puntaje.getRachaDias(),
                puntaje.getRachaPartidaActual(),
                usuario.getComodinesRacha(),
                insigniasNuevas
        );
    }

    /**
     * Activa el comodín de racha congelada para no perder la racha si no se juega al día siguiente.
     */
    @Transactional
    public boolean congelarRacha() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getComodinesRacha() <= 0) {
            return false;
        }

        Puntaje puntaje = puntajeRepository.findByUsuarioId(usuario.getId())
                .orElse(new Puntaje(usuario));

        if (puntaje.isRachaCongelada()) {
            return true; // ya estaba congelada
        }

        usuario.setComodinesRacha(usuario.getComodinesRacha() - 1);
        usuarioRepository.save(usuario);

        puntaje.setRachaCongelada(true);
        puntajeRepository.save(puntaje);
        return true;
    }

    /**
     * Posición real del usuario autenticado en el ranking global.
     */
    public MiPosicionDTO getMiPosicion() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String insignia = usuario.getInsigniaEquipada() != null ? usuario.getInsigniaEquipada().getIcono() + " " + usuario.getInsigniaEquipada().getNombre() : null;

        return puntajeRepository.findByUsuarioId(usuario.getId())
                .map(p -> new MiPosicionDTO(
                        puntajeRepository.findPosicion(usuario.getId()),
                        p.getPuntos(),
                        p.getPartidasJugadas(),
                        p.getPartidasCorrectas(),
                        p.getRachaDias(),
                        p.getPuntosPeliculas(),
                        p.getPuntosMusica(),
                        p.getPuntosDeportes(),
                        p.getRachaPartidaMaxima(),
                        p.isRachaCongelada(),
                        usuario.getComodinesRacha(),
                        insignia))
                .orElse(new MiPosicionDTO(0, 0, 0, 0, 0, 0, 0, 0, 0, false, usuario.getComodinesRacha(), insignia));
    }

    /**
     * Rankings generales y diarios
     */
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

    public List<RankingDTO> getRankingDiario() {
        List<Object[]> rows = partidaRepository.findTop10Diario(LocalDate.now());
        List<RankingDTO> ranking = new ArrayList<>();
        int currentRank = 1;
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            int puntos = ((Number) row[1]).intValue();
            if (i > 0) {
                int prevPuntos = ((Number) rows.get(i - 1)[1]).intValue();
                if (puntos < prevPuntos) {
                    currentRank = i + 1;
                }
            }
            ranking.add(new RankingDTO(
                    currentRank,
                    (String) row[4],
                    puntos,
                    ((Number) row[2]).intValue(),
                    ((Number) row[3]).intValue(),
                    ((Number) row[5]).intValue()
            ));
        }
        return ranking;
    }

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
