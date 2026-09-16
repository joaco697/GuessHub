package com.guesshub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.dto.PeliculaDTO;
import com.guesshub.service.PeliculaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pelicula")
@RequiredArgsConstructor
public class PeliculaController {

    private final PeliculaService peliculaService;

    /**
     * GET /api/pelicula/random
     * Devuelve una película aleatoria con pistas (nunca el título: ver PreguntaActivaService).
     */
    @GetMapping("/random")
    public ResponseEntity<PeliculaDTO> obtenerPeliculaRandom() {
        try {
            return ResponseEntity.ok(peliculaService.obtenerPeliculaRandom());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
