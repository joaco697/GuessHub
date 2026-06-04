package com.guesshub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RespuestaDTO(
    @NotNull(message = "El id de la película es requerido")
    Long peliculaId,
    @NotBlank(message = "La respuesta no puede estar vacía")
    String respuesta
) {}