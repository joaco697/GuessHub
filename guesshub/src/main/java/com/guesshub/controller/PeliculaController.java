package com.guesshub.controller;

import com.guesshub.dto.PeliculaDTO;
import com.guesshub.service.PeliculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PeliculaController {

    private final PeliculaService peliculaService;

    /**
     * GET /api/pelicula/random
     * Devuelve una película aleatoria de la BD buscada en OMDB.
     */
    @GetMapping("/pelicula/random")
    public ResponseEntity<List<PeliculaDTO>> obtenerPeliculaRandom() {
        try {
            List<PeliculaDTO> peliculas = peliculaService.obtenerPeliculaRandom();
            if (peliculas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(peliculas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}