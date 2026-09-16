package com.guesshub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "canciones",
       uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "artista"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String artista;

    public Cancion(String nombre, String artista) {
        this.nombre = nombre;
        this.artista = artista;
    }
}