package com.guesshub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancionDTO {
    private String nombre;
    private String artista;
    private String pista1;   // artista
    private String pista2;   // álbum
    private String pista3;   // fecha de lanzamiento
    private String logo;     // cover del álbum
    private String preview;  // URL preview de 30s
}