package com.guesshub.controller;

import com.guesshub.dto.CancionDTO;
import com.guesshub.service.EquipoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    /**
     * GET /api/random
     * Devuelve una canción aleatoria de la BD buscada en Deezer.
     */
    @GetMapping("/random")
    public ResponseEntity<List<CancionDTO>> obtenerCancionRandom() {
        try {
            List<CancionDTO> canciones = equipoService.obtenerTrackRandom();
            if (canciones.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(canciones);
        } catch (RuntimeException e) {
            // BD vacía u otro error de negocio
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}