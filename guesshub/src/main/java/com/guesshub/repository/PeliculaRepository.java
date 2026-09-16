package com.guesshub.repository;

import com.guesshub.entity.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    boolean existsByTituloOriginal(String tituloOriginal);

    @Query(value = "SELECT * FROM peliculas ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Pelicula> findRandom();

    @Query(value = "SELECT * FROM peliculas WHERE LOWER(titulo) LIKE LOWER(CONCAT('%', :q, '%')) LIMIT 10", nativeQuery = true)
    List<Pelicula> searchByTitulo(@Param("q") String q);
}