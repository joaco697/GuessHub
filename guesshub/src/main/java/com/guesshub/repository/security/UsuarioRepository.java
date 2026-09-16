package com.guesshub.repository.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guesshub.entity.security.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByDisplayName(String displayName);
}