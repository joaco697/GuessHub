package com.guesshub.dto;

import java.time.LocalDate;
import java.util.List;

public record DesafioDiarioDTO(
    String id,
    LocalDate fecha,
    String categoria,
    String nombreCategoria,
    String icono,
    String titulo,
    List<String> pistas,
    List<String> etiquetasPistas,
    String dificultad,
    boolean jugadoHoy
) {}

