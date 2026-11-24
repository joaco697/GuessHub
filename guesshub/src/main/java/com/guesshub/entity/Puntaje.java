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

    @Column(name = "partidas_jugadas", columnDefinition = "integer default 0")
    private int partidasJugadas = 0;

    @Column(name = "partidas_correctas", columnDefinition = "integer default 0")
    private int partidasCorrectas = 0;

    // ── Racha global por días consecutivos ─────────────────────────────────
    @Column(name = "racha_dias", columnDefinition = "integer default 0")
    private int rachaDias = 0;

    @Column(name = "ultima_partida_fecha")
    private LocalDate ultimaPartidaFecha;

    // Flag que indica si la racha fue congelada para evitar perderla
    @Column(name = "racha_congelada", columnDefinition = "boolean default false")
    private boolean rachaCongelada = false;

    // ── Racha por partida (respuestas correctas consecutivas) ──────────────
    @Column(name = "racha_partida_actual", columnDefinition = "integer default 0")
    private int rachaPartidaActual = 0;

    @Column(name = "racha_partida_maxima", columnDefinition = "integer default 0")
    private int rachaPartidaMaxima = 0;

    // ── Puntaje por categoría ──────────────────────────────────────────────
    @Column(name = "puntos_peliculas", columnDefinition = "integer default 0")
    private int puntosPeliculas = 0;

    @Column(name = "puntos_musica", columnDefinition = "integer default 0")
    private int puntosMusica = 0;

    @Column(name = "puntos_deportes", columnDefinition = "integer default 0")
    private int puntosDeportes = 0;

    public Puntaje(Usuario usuario) {
        this.usuario = usuario;
        this.puntos = 0;
        this.partidasJugadas = 0;
        this.partidasCorrectas = 0;
        this.rachaDias = 0;
        this.ultimaPartidaFecha = null;
        this.rachaCongelada = false;
        this.rachaPartidaActual = 0;
        this.rachaPartidaMaxima = 0;
        this.puntosPeliculas = 0;
        this.puntosMusica = 0;
        this.puntosDeportes = 0;
    }
}
