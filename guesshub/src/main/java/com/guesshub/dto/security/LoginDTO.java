package com.guesshub.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginDTO(
        @NotBlank(message = "El nombre de usuario es requerido")
        @Email(message = "El nombre de usuario debe ser un email válido") 
        String username,
        @NotBlank(message = "La contraseña es requerida") 
        @Pattern(regexp = "^.{8,}$", message = "La contraseña debe tener al menos 8")
        String password) {
}
