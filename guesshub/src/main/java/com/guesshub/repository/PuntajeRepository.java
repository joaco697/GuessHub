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
     * Posición del usuario en el ranking global con empate compartido:
     * si dos usuarios tienen el mismo puntaje, comparten posición.
     */
    @Query(value = """
        SELECT COUNT(*) + 1
        FROM puntajes p2
        WHERE p2.puntos > (SELECT p.puntos FROM puntajes p WHERE p.usuario_id = :usuarioId)
        """, nativeQuery = true)
    int findPosicion(@Param("usuarioId") Long usuarioId);

    /**
     * Top 10 global.
     * Columnas: [0]=id [1]=usuario_id [2]=puntos [3]=partidas_jugadas [4]=partidas_correctas [5]=display_name [6]=racha_dias
     */
    @Query(value = """
        SELECT p.id,
               p.usuario_id,
               p.puntos,
               p.partidas_jugadas,
               p.partidas_correctas,
               u.display_name,
               p.racha_dias
        FROM puntajes p
        JOIN usuarios u ON u.id = p.usuario_id
        ORDER BY p.puntos DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10();

    /**
     * Top 10 por categoría películas.
     * Columnas: [0]=id [1]=usuario_id [2]=puntos_peliculas [3]=partidas_jugadas [4]=partidas_correctas [5]=display_name [6]=racha_dias
     */
    @Query(value = """
        SELECT p.id,
               p.usuario_id,
               p.puntos_peliculas,
               p.partidas_jugadas,
               p.partidas_correctas,
               u.display_name,
               p.racha_dias
        FROM puntajes p
        JOIN usuarios u ON u.id = p.usuario_id
        WHERE p.puntos_peliculas > 0
        ORDER BY p.puntos_peliculas DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10Peliculas();

    /**
     * Top 10 por categoría música.
     */
    @Query(value = """
        SELECT p.id,
               p.usuario_id,
               p.puntos_musica,
               p.partidas_jugadas,
               p.partidas_correctas,
               u.display_name,
               p.racha_dias
        FROM puntajes p
        JOIN usuarios u ON u.id = p.usuario_id
        WHERE p.puntos_musica > 0
        ORDER BY p.puntos_musica DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10Musica();

    /**
     * Top 10 por categoría deportes.
     */
    @Query(value = """
        SELECT p.id,
               p.usuario_id,
               p.puntos_deportes,
               p.partidas_jugadas,
               p.partidas_correctas,
               u.display_name,
               p.racha_dias
        FROM puntajes p
        JOIN usuarios u ON u.id = p.usuario_id
        WHERE p.puntos_deportes > 0
        ORDER BY p.puntos_deportes DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTop10Deportes();
}