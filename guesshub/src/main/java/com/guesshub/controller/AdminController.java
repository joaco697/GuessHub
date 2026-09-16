package com.guesshub.controller;

import com.guesshub.service.LastFmService;
import com.guesshub.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LastFmService lastFmService;
    private final TmdbService tmdbService;

    /**
     * POST /api/admin/sync
     * Sincroniza top 100 canciones desde Last.fm → Supabase
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncLastFm() {
        try {
            int nuevas = lastFmService.syncTop100();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "cancionesNuevas", nuevas,
                    "mensaje", nuevas > 0
                            ? nuevas + " canciones nuevas guardadas"
                            : "No hay canciones nuevas (ya estaban todas)"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "ok", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/admin/sync-peliculas
     * Sincroniza top 100 películas desde TMDB → Supabase
     */
    @PostMapping("/sync-peliculas")
    public ResponseEntity<Map<String, Object>> syncTmdb() {
        try {
            int nuevas = tmdbService.syncTop100();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "peliculasNuevas", nuevas,
                    "mensaje", nuevas > 0
                            ? nuevas + " películas nuevas guardadas"
                            : "No hay películas nuevas (ya estaban todas)"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "ok", false,
                    "error", e.getMessage()
            ));
        }
    }
}