package com.guesshub.controller;

import com.guesshub.dto.RespuestaDTO;
import com.guesshub.dto.ResultadoDTO;
import com.guesshub.service.PreguntaActivaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint único de validación de respuestas para Películas, Música y Deportes.
 * Reemplaza la validación client-side que tenían Música y Deportes: ahora las
 * tres categorías comparan contra la respuesta que el servidor guardó al
 * generar la pregunta (ver PreguntaActivaService), nunca contra un valor que
 * viajó al navegador.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RespuestaController {

    private final PreguntaActivaService preguntaActivaService;

    /**
     * POST /api/respuesta
     * Body: { "preguntaId": "...", "respuesta": "..." }
     */
    @PostMapping("/respuesta")
    public ResponseEntity<ResultadoDTO> validarRespuesta(@Valid @RequestBody RespuestaDTO dto) {
        return ResponseEntity.ok(preguntaActivaService.validar(dto.preguntaId(), dto.respuesta()));
    }
}
