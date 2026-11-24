package com.guesshub.dto;

public record MiPosicionDTO(
    int posicion,
    int puntos,
    int partidasJugadas,
    int partidasCorrectas,
    int rachaDias,
    int puntosPeliculas,
    int puntosMusica,
    int puntosDeportes,
    int rachaPartidaMaxima,
    boolean rachaCongelada,
    int comodinesRacha,
    String insigniaEquipada
) {
    public MiPosicionDTO(int posicion, int puntos, int partidasJugadas, int partidasCorrectas,
                         int rachaDias, int puntosPeliculas, int puntosMusica, int puntosDeportes) {
        this(posicion, puntos, partidasJugadas, partidasCorrectas, rachaDias,
             puntosPeliculas, puntosMusica, puntosDeportes, 0, false, 1, null);
    }
}
