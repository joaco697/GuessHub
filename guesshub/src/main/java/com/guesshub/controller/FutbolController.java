package com.guesshub.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.dto.EquipoDTO;
import com.guesshub.service.FutbolService;

/**
 * Controller para la categoría Deportes / Fútbol.
 * Devuelve equipos de fútbol aleatorios y sus pistas.
 */
@RestController
@RequestMapping({"/equipo", "/api/futbol", "/api/deportes"})
@RequiredArgsConstructor
public class FutbolController {

    private final FutbolService futbolService;

    /**
     * GET /equipo/random, /api/futbol/random, /api/deportes/random
     */
    @GetMapping("/random")
    public ResponseEntity<List<EquipoDTO>> obtenerEquipoRandom() {
        try {
            List<EquipoDTO> equipos = futbolService.obtenerEquipoRandom();
            if (equipos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(equipos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}