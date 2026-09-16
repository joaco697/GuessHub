package com.guesshub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NUNCA incluye el nombre de la canción: la respuesta correcta vive solo en
 * el servidor (PreguntaActivaService), identificada por "id" (UUID).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancionDTO {
    private String id;
    private String pista1;   // artista
    private String pista2;   // álbum
    private String pista3;   // fecha de lanzamiento
    private String logo;     // cover del álbum
    private String preview;  // URL preview de 30s
}
