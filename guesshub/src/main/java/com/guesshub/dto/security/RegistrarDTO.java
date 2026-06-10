package com.guesshub.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrarDTO(
        @NotBlank(message = "El nombre de usuario es requerido")
        @Email(message = "El nombre de usuario debe ser un email válido")
        String username,

        @NotBlank(message = "El nombre visible es requerido")
        @Size(min = 3, max = 25, message = "El nombre visible debe tener entre 3 y 25 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_\\-\\.]+$", message = "El nombre visible solo puede tener letras, números, _, - y .")
        String displayName,

        @NotBlank(message = "La contraseña es requerida")
        @Pattern(regexp = "^.{8,}$",
                 message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotBlank(message = "La verificación de contraseña es requerida")
        @Pattern(regexp = "^.{8,}$",
                 message = "La verificación de contraseña debe tener al menos 8 caracteres")
        String verificacionPassword) {
}