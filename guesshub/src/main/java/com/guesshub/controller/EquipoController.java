package com.guesshub.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.dto.EquipoDTO;
import com.guesshub.service.EquipoService;

@RestController
@RequestMapping("/api")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    /**
     * GET http://localhost:8080/api/random
     */
    @GetMapping("/random")
    public ResponseEntity<List<EquipoDTO>> obtenerCancionRandom() {

        try {

            List<EquipoDTO> canciones = equipoService.obtenerEquiposPorNombre("");

            if (canciones.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(canciones);

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}