package com.guesshub.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarPasswordDTO(
    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe ser un email válido")
    String email
) {}
