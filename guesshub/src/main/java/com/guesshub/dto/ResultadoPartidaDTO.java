package com.guesshub.dto;

import java.util.List;

public record ResultadoPartidaDTO(
    int puntosGanados,
    double multiplicador,
    int rachaDias,
    int rachaPartida,
    int comodinesRacha,
    List<String> insigniasNuevas
) {}

