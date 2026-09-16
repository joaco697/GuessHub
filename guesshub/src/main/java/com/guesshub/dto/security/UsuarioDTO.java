package com.guesshub.dto.security;

import java.util.List;

public record UsuarioDTO(String username, String displayName, List<String> roles) {
}