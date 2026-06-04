package com.guesshub.repository;

import com.guesshub.entity.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    boolean existsByTituloOriginal(String tituloOriginal);

    @Query(value = "SELECT * FROM peliculas ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Pelicula> findRandom();
}