package com.guesshub.dto;

/**
 * @param correcto      si la respuesta fue correcta
 * @param pistasUsadas  cantidad de pistas reveladas (1, 2 o 3)
 * @param segundosUsados tiempo en segundos que tardó en responder
 * @param categoria     categoría del juego: "pelicula", "cancion" o "equipo"
 */
public record PartidaDTO(
    boolean correcto,
    int pistasUsadas,
    int segundosUsados,
    String categoria
) {}