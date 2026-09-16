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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PuntajeService {

    private final PuntajeRepository puntajeRepository;
    private final UsuarioRepository usuarioRepository;

    public int calcularPuntos(boolean correcto, int pistasUsadas, int segundosUsados) {
        if (!correcto) return 0;
        int puntos = 1000;
        if (pistasUsadas >= 2) puntos -= 200;
        if (pistasUsadas >= 3) puntos -= 200;
        puntos -= (segundosUsados * 5);
        return Math.max(0, puntos);
    }

    @Transactional
    public int guardarPartida(boolean correcto, int pistasUsadas, int segundosUsados) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        int puntosGanados = calcularPuntos(correcto, pistasUsadas, segundosUsados);

        Puntaje puntaje = puntajeRepository.findByUsuarioId(usuario.getId())
                .orElse(new Puntaje(usuario));

        puntaje.setPuntos(puntaje.getPuntos() + puntosGanados);
        puntaje.setPartidasJugadas(puntaje.getPartidasJugadas() + 1);
        if (correcto) puntaje.setPartidasCorrectas(puntaje.getPartidasCorrectas() + 1);

        puntajeRepository.save(puntaje);
        log.info("Partida guardada para {}: +{} pts", username, puntosGanados);

        return puntosGanados;
    }

    /**
     * Posición real del usuario autenticado, aunque esté fuera del Top 10.
     * Si todavía no jugó ninguna partida, devuelve todo en 0 y sin posición calculable.
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
                        p.getPartidasCorrectas()))
                .orElse(new MiPosicionDTO(0, 0, 0, 0));
    }

    /**
     * Query devuelve: [0]=id [1]=usuario_id [2]=puntos [3]=partidas_jugadas [4]=partidas_correctas [5]=display_name
     */
    public List<RankingDTO> getRanking() {
        List<Object[]> rows = puntajeRepository.findTop10();
        List<RankingDTO> ranking = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            ranking.add(new RankingDTO(
                i + 1,
                (String) row[5],              // display_name
                ((Number) row[2]).intValue(),  // puntos
                ((Number) row[3]).intValue(),  // partidas_jugadas
                ((Number) row[4]).intValue()   // partidas_correctas
            ));
        }
        return ranking;
    }
}