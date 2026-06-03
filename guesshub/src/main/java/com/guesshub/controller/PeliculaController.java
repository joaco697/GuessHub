package com.guesshub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.dto.PeliculaDTO;
import com.guesshub.service.PeliculaService;

@RestController
@RequestMapping("/pelicula")
public class PeliculaController {

    @Autowired
    private PeliculaService peliculaService;

    /** http://localhost:8080/pelicula/random */
    @GetMapping("/random")
    public ResponseEntity<List<PeliculaDTO>> obtenerPeliculaRandom() {

        try {

            List<PeliculaDTO> peliculaes = peliculaService.obtenerPeliculaRandom();

            if (peliculaes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(peliculaes);

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}