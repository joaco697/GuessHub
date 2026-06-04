package com.guesshub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.dto.PeliculaDTO;
import com.guesshub.dto.RespuestaDTO;
import com.guesshub.dto.ResultadoDTO;
import com.guesshub.service.PeliculaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pelicula")
@RequiredArgsConstructor
public class PeliculaController {

    private final PeliculaService peliculaService;

    /**
     * GET /api/pelicula/random
     * Devuelve una película aleatoria con pistas (sin revelar el título).
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

    /**
     * POST /api/pelicula/respuesta
     * Recibe { peliculaId, respuesta } y valida contra la BD.
     */
    @PostMapping("/respuesta")
    public ResponseEntity<ResultadoDTO> validarRespuesta(@Valid @RequestBody RespuestaDTO dto) {
        try {
            return ResponseEntity.ok(peliculaService.validarRespuesta(dto.peliculaId(), dto.respuesta()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}