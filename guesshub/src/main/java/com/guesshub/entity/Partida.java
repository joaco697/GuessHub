package com.guesshub.entity;

import com.guesshub.entity.security.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "partidas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();

    @Column(nullable = false)
    private LocalDateTime momento = LocalDateTime.now();

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private int puntos = 0;

    @Column(nullable = false)
    private boolean correcto = false;

    @Column(name = "pistas_usadas", nullable = false)
    private int pistasUsadas = 1;

    @Column(name = "segundos_usados", nullable = false)
    private int segundosUsados = 0;

    @Column(length = 20)
    private String dificultad = "FACIL";

    @Column(name = "es_desafio_diario", nullable = false)
    private boolean esDesafioDiario = false;

    @Column(nullable = false)
    private double multiplicador = 1.0;

    public Partida(Usuario usuario, String categoria, int puntos, boolean correcto,
                   int pistasUsadas, int segundosUsados, String dificultad,
                   boolean esDesafioDiario, double multiplicador) {
        this.usuario = usuario;
        this.categoria = categoria;
        this.puntos = puntos;
        this.correcto = correcto;
        this.pistasUsadas = pistasUsadas;
        this.segundosUsados = segundosUsados;
        this.dificultad = dificultad != null ? dificultad : "FACIL";
        this.esDesafioDiario = esDesafioDiario;
        this.multiplicador = multiplicador;
        this.fecha = LocalDate.now();
        this.momento = LocalDateTime.now();
    }
}

