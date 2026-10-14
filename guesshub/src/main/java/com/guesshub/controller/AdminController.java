package com.guesshub.controller;

import com.guesshub.service.LastFmService;
import com.guesshub.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller de administración.
 * Permite sincronizar catálogos offline/backup en base de datos
 * y verificar el estado de los servicios en vivo (TMDB, Last.fm, Deezer, etc.).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LastFmService lastFmService;
    private final TmdbService tmdbService;

    /**
     * GET /api/admin/status
     * Informa el estado y modo operativo del backend (LIVE_API).
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> estadoSistema() {
        return ResponseEntity.ok(Map.of(
                "modoJuego", "LIVE_API",
                "fuentes", Map.of(
                        "peliculas", "TMDB_LIVE + OMDB (en vivo, sin almacenamiento de títulos en cliente)",
                        "musica", "LASTFM_LIVE + DEEZER (en vivo)",
                        "deportes", "APISPORTS_LIVE + FALLBACK (en vivo con fallback local)"
                ),
                "validacionRespuestas", "SERVER_SIDE_CENTRALIZADA"
        ));
    }

    /**
     * GET /api/admin/test-live
     * Prueba en tiempo real la conectividad de los servicios live (TMDB y Last.fm).
     */
    @GetMapping("/test-live")
    public ResponseEntity<Map<String, Object>> testLiveApis() {
        boolean lastFmOk = false;
        boolean tmdbOk = false;
        String lastFmError = null;
        String tmdbError = null;

        try {
            var cancionLive = lastFmService.obtenerAleatoriaLive();
            lastFmOk = cancionLive != null && cancionLive.nombre() != null;
        } catch (Exception e) {
            lastFmError = e.getMessage();
        }

        try {
            var peliculaLive = tmdbService.obtenerAleatoriaLive();
            tmdbOk = peliculaLive != null && peliculaLive.titulo() != null;
        } catch (Exception e) {
            tmdbError = e.getMessage();
        }

        return ResponseEntity.ok(Map.of(
                "ok", lastFmOk && tmdbOk,
                "lastFmLive", Map.of("activo", lastFmOk, "error", lastFmError != null ? lastFmError : "none"),
                "tmdbLive", Map.of("activo", tmdbOk, "error", tmdbError != null ? tmdbError : "none")
        ));
    }

    /**
     * POST /api/admin/sync
     * Sincroniza top 100 canciones desde Last.fm → Supabase como catálogo/caché offline.
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncLastFm() {
        try {
            int nuevas = lastFmService.syncTop100();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "modo", "catalogo_offline_cache",
                    "cancionesNuevas", nuevas,
                    "mensaje", nuevas > 0
                            ? nuevas + " canciones nuevas guardadas en BD como catálogo offline"
                            : "No hay canciones nuevas (ya estaban todas en BD)"
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
     * Sincroniza top 100 películas desde TMDB → Supabase como catálogo/caché offline.
     */
    @PostMapping("/sync-peliculas")
    public ResponseEntity<Map<String, Object>> syncTmdb() {
        try {
            int nuevas = tmdbService.syncTop100();
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "modo", "catalogo_offline_cache",
                    "peliculasNuevas", nuevas,
                    "mensaje", nuevas > 0
                            ? nuevas + " películas nuevas guardadas en BD como catálogo offline"
                            : "No hay películas nuevas (ya estaban todas en BD)"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "ok", false,
                    "error", e.getMessage()
            ));
        }
    }
}