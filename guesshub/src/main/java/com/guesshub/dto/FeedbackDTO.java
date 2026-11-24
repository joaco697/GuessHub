package com.guesshub.dto;

import jakarta.validation.constraints.NotBlank;

public record FeedbackDTO(
    String emailContacto,
    @NotBlank(message = "El tipo de feedback es requerido")
    String tipo,
    @NotBlank(message = "La opción guiada es requerida")
    String opcionGuiada,
    String comentario
) {}

