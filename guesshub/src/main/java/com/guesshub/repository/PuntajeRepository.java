package com.guesshub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guesshub.entity.Puntaje;

public interface PuntajeRepository extends JpaRepository<Puntaje, Long> {

    Optional<Puntaje> findByUsuarioId(Long usuarioId);

    @Query(value = """
        SELECT p.*, u.username
        FROM puntajes p
        JOIN usuarios u ON u.id = p.usuario_id
        ORDER BY p.puntos DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10();
}