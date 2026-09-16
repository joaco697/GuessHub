package com.guesshub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.guesshub.entity.Puntaje;

public interface PuntajeRepository extends JpaRepository<Puntaje, Long> {

    Optional<Puntaje> findByUsuarioId(Long usuarioId);

    /**
     * Posición del usuario en el ranking global, considerando empates:
     * si dos usuarios tienen el mismo puntaje, comparten posición.
     * (cantidad de usuarios con MÁS puntos que él + 1)
     */
    @Query(value = """
        SELECT COUNT(*) + 1
        FROM puntajes p2
        WHERE p2.puntos > (SELECT p.puntos FROM puntajes p WHERE p.usuario_id = :usuarioId)
        """, nativeQuery = true)
    int findPosicion(@Param("usuarioId") Long usuarioId);

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