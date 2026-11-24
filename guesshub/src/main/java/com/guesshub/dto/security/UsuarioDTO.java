package com.guesshub.dto.security;

import java.util.List;

public record UsuarioDTO(
    String username,
    String displayName,
    String fotoPerfil,
    int comodinesRacha,
    String insigniaEquipada,
    List<String> roles
) {
    public UsuarioDTO(String username, String displayName, List<String> roles) {
        this(username, displayName, "👤", 1, null, roles);
    }
}
