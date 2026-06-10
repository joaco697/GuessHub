package com.guesshub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guesshub.entity.Puntaje;

public interface PuntajeRepository extends JpaRepository<Puntaje, Long> {

    Optional<Puntaje> findByUsuarioId(Long usuarioId);

    // Columnas explícitas: [0]=id [1]=usuario_id [2]=puntos [3]=partidas_jugadas [4]=partidas_correctas [5]=display_name
    @Query(value = """
        SELECT p.id,
               p.usuario_id,
               p.puntos,
               p.partidas_jugadas,
               p.partidas_correctas,
               u.display_name
        FROM puntajes p
        JOIN usuarios u ON u.id = p.usuario_id
        ORDER BY p.puntos DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10();
}