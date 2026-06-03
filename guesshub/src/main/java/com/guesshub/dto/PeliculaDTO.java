
package com.guesshub.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PeliculaDTO {

    private int id;
    private String nombre;
    private String genero;
    private int año;
    private String poster;
    private String director;


}       