package com.guesshub.repository.security;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guesshub.entity.security.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}

