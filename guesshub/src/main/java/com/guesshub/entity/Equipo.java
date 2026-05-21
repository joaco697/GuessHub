package com.guesshub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "equipos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    @Id
    private int id;

    private String nombre;
    private String pista1;
    private String pista2;
    private String logo;
    private String pista3;
    public Equipo(String nombre) {
        this.nombre = nombre;
    }
}

