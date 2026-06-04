package com.guesshub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeliculaDTO {
    private Long id;
    private String titulo;
    private String pista1;
    private String pista2;
    private String pista3;
    private String poster;
    private String sinopsis;
}