package com.guesshub.dto.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerPasswordDTO(
    @NotBlank(message = "El token o código es requerido")
    String token,

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String nuevaPassword,

    @NotBlank(message = "La confirmación de la contraseña es requerida")
    String confirmacionPassword
) {}
