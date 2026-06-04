package com.guesshub.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeliculaDTO {
    private String titulo;
    private String pista1;   // género
    private String pista2;   // director
    private String pista3;   // año
    private String poster;   // URL del poster
    private String sinopsis; // para pista extra si se quiere
}