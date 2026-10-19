package com.guesshub.entity;

import com.guesshub.entity.security.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "puntajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Puntaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private int puntos = 0;

    @Column(name = "partidas_jugadas", nullable = false)
    private int partidasJugadas = 0;

    @Column(name = "partidas_correctas", nullable = false)
    private int partidasCorrectas = 0;

    // ── Racha ──────────────────────────────────────────────────────────────
    /** Días consecutivos que el usuario jugó al menos una partida */
    @Column(name = "racha_dias", nullable = false)
    private int rachaDias = 0;

    /** Fecha de la última partida jugada (para calcular si la racha sigue) */
    @Column(name = "ultima_partida_fecha")
    private LocalDate ultimaPartidaFecha;

    // ── Puntaje por categoría ──────────────────────────────────────────────
    @Column(name = "puntos_peliculas", nullable = false)
    private int puntosPeliculas = 0;

    @Column(name = "puntos_musica", nullable = false)
    private int puntosMusica = 0;

    @Column(name = "puntos_deportes", nullable = false)
    private int puntosDeportes = 0;

    public Puntaje(Usuario usuario) {
        this.usuario = usuario;
        this.puntos = 0;
        this.partidasJugadas = 0;
        this.partidasCorrectas = 0;
        this.rachaDias = 0;
        this.ultimaPartidaFecha = null;
        this.puntosPeliculas = 0;
        this.puntosMusica = 0;
        this.puntosDeportes = 0;
    }
}