package com.guesshub.repository;

import com.guesshub.entity.DesafioDiario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DesafioDiarioRepository extends JpaRepository<DesafioDiario, Long> {
    Optional<DesafioDiario> findByFecha(LocalDate fecha);
}

