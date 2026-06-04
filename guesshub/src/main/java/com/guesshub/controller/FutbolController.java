package com.guesshub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.dto.EquipoDTO;
import com.guesshub.service.FutbolService;

@RestController
@RequestMapping("/equipo")
public class FutbolController {

    @Autowired
    private FutbolService equipoService;

    /**
     * GET http://localhost:8080/equipo/random
     */
    @GetMapping("/random")
    public ResponseEntity<List<EquipoDTO>> obtenerEquipoRandom() {

        try {

            List<EquipoDTO> equipos =
                    equipoService.obtenerEquipoRandom();

            if (equipos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(equipos);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}