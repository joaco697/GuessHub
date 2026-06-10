package com.guesshub.controller;

import com.guesshub.dto.PartidaDTO;
import com.guesshub.dto.RankingDTO;
import com.guesshub.service.PuntajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PuntajeController {

    private final PuntajeService puntajeService;

    /**
     * POST /api/partida
     * Guarda el resultado de una partida y devuelve los puntos ganados.
     */
    @PostMapping("/partida")
    public ResponseEntity<Map<String, Object>> guardarPartida(@RequestBody PartidaDTO dto) {
        try {
            int puntos = puntajeService.guardarPartida(dto.correcto(), dto.pistasUsadas(), dto.segundosUsados());
            return ResponseEntity.ok(Map.of("puntosGanados", puntos));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/ranking
     * Devuelve el top 10 global.
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingDTO>> getRanking() {
        return ResponseEntity.ok(puntajeService.getRanking());
    }
}