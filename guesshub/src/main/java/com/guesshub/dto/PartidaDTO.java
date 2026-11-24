package com.guesshub.dto;

/**
 * @param correcto        si la respuesta fue correcta
 * @param pistasUsadas    cantidad de pistas reveladas (1, 2 o 3)
 * @param segundosUsados  tiempo en segundos que tardó en responder
 * @param categoria       categoría del juego: "pelicula", "cancion" o "equipo"
 * @param dificultad      dificultad: "FACIL", "MEDIO", "DIFICIL" (default: "FACIL")
 * @param rachaPartida    respuestas correctas consecutivas en la sesión actual
 * @param esDesafioDiario true si la partida corresponde al minijuego del día
 */
public record PartidaDTO(
    boolean correcto,
    int pistasUsadas,
    int segundosUsados,
    String categoria,
    String dificultad,
    Integer rachaPartida,
    Boolean esDesafioDiario
) {
    public PartidaDTO(boolean correcto, int pistasUsadas, int segundosUsados, String categoria) {
        this(correcto, pistasUsadas, segundosUsados, categoria, "FACIL", 0, false);
    }
}
