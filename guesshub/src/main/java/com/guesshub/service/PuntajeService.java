package com.guesshub.service;

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

    /**
     * Calcula los puntos de una partida:
     * - Base: 1000 pts si acertó, 0 si no
     * - Penalización pistas: -200 por cada pista extra (pista2 = -200, pista3 = -400)
     * - Penalización tiempo: -5 pts por cada segundo usado
     * Mínimo 0 puntos.
     */
    public int calcularPuntos(boolean correcto, int pistasUsadas, int segundosUsados) {
        if (!correcto) return 0;

        int puntos = 1000;

        // Penalización por pistas (pista 1 es gratis, pista 2 = -200, pista 3 = -400)
        if (pistasUsadas >= 2) puntos -= 200;
        if (pistasUsadas >= 3) puntos -= 200;

        // Penalización por tiempo (5 pts por segundo)
        puntos -= (segundosUsados * 5);

        return Math.max(0, puntos);
    }

    /**
     * Guarda el resultado de una partida para el usuario autenticado.
     * Crea el registro de puntaje si no existe.
     */
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
        log.info("Partida guardada para {}: +{} pts (correcto={}, pistas={}, segundos={})",
                username, puntosGanados, correcto, pistasUsadas, segundosUsados);

        return puntosGanados;
    }

    /**
     * Devuelve el top 10 del ranking global.
     */
    public List<RankingDTO> getRanking() {
        List<Object[]> rows = puntajeRepository.findTop10();
        List<RankingDTO> ranking = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            ranking.add(new RankingDTO(
                i + 1,
                (String) row[5],              // username
                ((Number) row[2]).intValue(),  // puntos
                ((Number) row[3]).intValue(),  // partidas_jugadas
                ((Number) row[4]).intValue()   // partidas_correctas
            ));
        }
        return ranking;
    }
}