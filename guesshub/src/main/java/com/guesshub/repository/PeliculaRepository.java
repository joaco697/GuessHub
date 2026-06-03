package com.guesshub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.guesshub.entity.Pelicula;

public interface PeliculaRepository extends JpaRepository<Pelicula, Integer> {}