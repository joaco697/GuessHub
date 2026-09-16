package com.guesshub.dto;

import jakarta.validation.constraints.NotBlank;

public record RespuestaDTO(
    @NotBlank(message = "El id de la pregunta es requerido")
    String preguntaId,
    @NotBlank(message = "La respuesta no puede estar vacía")
    String respuesta
) {}
