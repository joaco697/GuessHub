package com.guesshub.dto;

public record InsigniaDTO(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    String icono,
    String categoria,
    boolean desbloqueada,
    boolean equipada
) {}

