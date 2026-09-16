package com.guesshub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NUNCA incluye el título: la respuesta correcta vive solo en el servidor
 * (PreguntaActivaService), identificada por "id" (un UUID, no el id de la BD).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeliculaDTO {
    private String id;
    private String pista1;
    private String pista2;
    private String pista3;
    private String poster;
    private String sinopsis;
}
