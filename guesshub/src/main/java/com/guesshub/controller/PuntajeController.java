package com.guesshub.controller;

import com.guesshub.dto.MiPosicionDTO;
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
     * Guarda el resultado de una partida y devuelve los puntos ganados + racha actual.
     */
    @PostMapping("/partida")
    public ResponseEntity<Map<String, Object>> guardarPartida(@RequestBody PartidaDTO dto) {
        try {
            int puntos = puntajeService.guardarPartida(
                    dto.correcto(), dto.pistasUsadas(), dto.segundosUsados(), dto.categoria());
            return ResponseEntity.ok(Map.of("puntosGanados", puntos));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/ranking?cat=global|peliculas|musica|deportes
     * Devuelve el top 10. Sin parámetro o cat=global → ranking global.
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingDTO>> getRanking(
            @RequestParam(value = "cat", defaultValue = "global") String cat) {
        return switch (cat.toLowerCase()) {
            case "peliculas", "pelicula" -> ResponseEntity.ok(puntajeService.getRankingPeliculas());
            case "musica", "cancion"     -> ResponseEntity.ok(puntajeService.getRankingMusica());
            case "deportes", "equipo"    -> ResponseEntity.ok(puntajeService.getRankingDeportes());
            default                      -> ResponseEntity.ok(puntajeService.getRanking());
        };
    }

    /**
     * GET /api/ranking/mi-posicion
     * Devuelve la posición real del usuario autenticado, esté o no en el Top 10.
     */
    @GetMapping("/ranking/mi-posicion")
    public ResponseEntity<MiPosicionDTO> getMiPosicion() {
        try {
            return ResponseEntity.ok(puntajeService.getMiPosicion());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}