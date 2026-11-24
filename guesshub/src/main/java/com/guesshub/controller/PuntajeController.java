package com.guesshub.controller;

import com.guesshub.dto.MiPosicionDTO;
import com.guesshub.dto.PartidaDTO;
import com.guesshub.dto.RankingDTO;
import com.guesshub.dto.ResultadoPartidaDTO;
import com.guesshub.service.PuntajeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PuntajeController {

    private final PuntajeService puntajeService;

    /**
     * POST /api/partida
     * Guarda el resultado de una partida y devuelve los puntos ganados + multiplicador + racha + insignias.
     */
    @PostMapping("/partida")
    public ResponseEntity<?> guardarPartida(@RequestBody PartidaDTO dto) {
        try {
            ResultadoPartidaDTO resultado = puntajeService.registrarPartida(dto);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al guardar partida: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/racha/congelar
     * Congela la racha del usuario para protegerla un día sin jugar.
     */
    @PostMapping("/racha/congelar")
    public ResponseEntity<?> congelarRacha() {
        try {
            boolean exito = puntajeService.congelarRacha();
            if (exito) {
                return ResponseEntity.ok(Map.of("mensaje", "Racha congelada con éxito"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "No tienes comodines de congelamiento disponibles"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/ranking?cat=global|peliculas|musica|deportes&periodo=general|diario
     * Devuelve el top 10 general o del día de hoy.
     */
    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking(
            @RequestParam(value = "cat", defaultValue = "global") String cat,
            @RequestParam(value = "periodo", defaultValue = "general") String periodo) {
        try {
            if ("diario".equalsIgnoreCase(periodo) || "diario".equalsIgnoreCase(cat)) {
                return ResponseEntity.ok(puntajeService.getRankingDiario());
            }

            List<RankingDTO> ranking = switch (cat.toLowerCase()) {
                case "peliculas", "pelicula" -> puntajeService.getRankingPeliculas();
                case "musica", "cancion"     -> puntajeService.getRankingMusica();
                case "deportes", "equipo"    -> puntajeService.getRankingDeportes();
                default                      -> puntajeService.getRanking();
            };
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("Error al obtener ranking (cat={}, periodo={}): {}", cat, periodo, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
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
