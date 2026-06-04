package com.guesshub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "peliculas",
       uniqueConstraints = @UniqueConstraint(columnNames = {"titulo_original"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "titulo_original")
    private String tituloOriginal;

    public Pelicula(String titulo, String tituloOriginal) {
        this.titulo = titulo;
        this.tituloOriginal = tituloOriginal;
    }
}