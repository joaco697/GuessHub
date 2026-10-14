package com.guesshub.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record RespuestaDTO(
        @JsonAlias({"peliculaId", "id"})
        @NotBlank(message = "El id de la pregunta es requerido") String preguntaId,
        String respuesta) {
}
