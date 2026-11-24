package com.guesshub.repository;

import com.guesshub.entity.Insignia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsigniaRepository extends JpaRepository<Insignia, Long> {
    Optional<Insignia> findByCodigo(String codigo);
}

