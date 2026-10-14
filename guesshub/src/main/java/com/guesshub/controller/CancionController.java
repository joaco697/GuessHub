package com.guesshub.controller;

import com.guesshub.dto.CancionDTO;
import com.guesshub.service.CancionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller para la categoría Música.
 * Devuelve canciones aleatorias y pistas obtenidas en vivo desde Last.fm y Deezer.
 * Resuelve la confusión previa donde se utilizaba EquipoController para música.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CancionController {

    private final CancionService cancionService;

    /**
     * GET /api/random o /api/cancion/random
     * Devuelve una canción aleatoria con pistas (sin revelar el título).
     */
    @GetMapping({"/random", "/cancion/random"})
    public ResponseEntity<List<CancionDTO>> obtenerCancionRandom() {
        try {
            List<CancionDTO> canciones = cancionService.obtenerTrackRandom();
            if (canciones.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(canciones);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
