package com.guesshub.controller;

import com.guesshub.dto.DesafioDiarioDTO;
import com.guesshub.service.DesafioDiarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DesafioDiarioController {

    private final DesafioDiarioService desafioDiarioService;

    /**
     * GET /api/desafio-diario
     * Devuelve el desafío diario único para la fecha de hoy, compartido por todos los usuarios.
     */
    @GetMapping("/desafio-diario")
    public ResponseEntity<?> getDesafioDiario() {
        try {
            DesafioDiarioDTO dto = desafioDiarioService.obtenerDesafioHoy();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Error al obtener desafío diario: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "No se pudo obtener el desafío diario"));
        }
    }
}

