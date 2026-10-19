package com.guesshub.dto;

public record RankingDTO(
    int posicion,
    String username,
    int puntos,
    int partidasJugadas,
    int partidasCorrectas,
    int rachaDias
) {}