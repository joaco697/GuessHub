package com.guesshub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "insignias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Insignia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String icono;

    @Column(nullable = false, length = 50)
    private String categoria;

    public Insignia(String codigo, String nombre, String descripcion, String icono, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.categoria = categoria;
    }
}

