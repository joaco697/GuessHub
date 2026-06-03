package com.guesshub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.guesshub.entity.Cancion;

public interface CancionRepository extends JpaRepository<Cancion, Long> {

    boolean existsByNombreAndArtista(String nombre, String artista);

    // Devuelve una canción aleatoria de la tabla
    @Query(value = "SELECT * FROM canciones ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Cancion> findRandom();
}