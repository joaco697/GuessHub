package com.guesshub.dto;

public record ResultadoDTO(
    boolean correcto,
    String tituloCorrecto,
    String mensaje
) {}