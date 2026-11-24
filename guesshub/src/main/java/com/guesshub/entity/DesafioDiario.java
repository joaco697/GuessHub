package com.guesshub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "desafios_diarios",
       uniqueConstraints = @UniqueConstraint(columnNames = {"fecha"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesafioDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(name = "titulo_correcto", nullable = false)
    private String tituloCorrecto;

    @Column(name = "pista_1", nullable = false, length = 1000)
    private String pista1;

    @Column(name = "pista_2", length = 1000)
    private String pista2;

    @Column(name = "pista_3", length = 1000)
    private String pista3;

    @Column(name = "pista_2_etiqueta", length = 50)
    private String pista2Etiqueta;

    @Column(name = "pista_3_etiqueta", length = 50)
    private String pista3Etiqueta;

    @Column(length = 20)
    private String dificultad = "MEDIO";

    // preguntaId efímero en memoria para validación
    @Transient
    private String preguntaId;

    public DesafioDiario(LocalDate fecha, String categoria, String tituloCorrecto,
                         String pista1, String pista2, String pista3,
                         String pista2Etiqueta, String pista3Etiqueta, String dificultad) {
        this.fecha = fecha;
        this.categoria = categoria;
        this.tituloCorrecto = tituloCorrecto;
        this.pista1 = pista1;
        this.pista2 = pista2;
        this.pista3 = pista3;
        this.pista2Etiqueta = pista2Etiqueta;
        this.pista3Etiqueta = pista3Etiqueta;
        this.dificultad = dificultad;
    }
}

