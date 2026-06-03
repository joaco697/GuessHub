package com.guesshub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "peliculas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pelicula {

    @Id
    private int id;

    private String nombre;
    private String genero;
    private int año;
    private String poster;

    public Pelicula(String nombre) {
        this.nombre = nombre;
    }
}