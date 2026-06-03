package com.guesshub.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.service.LastFmService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LastFmService lastFmService;

    /**
     * POST /api/admin/sync
     * Requiere rol ADMIN.
     * Consulta Last.fm top 100 y guarda las canciones nuevas en Supabase.
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
}