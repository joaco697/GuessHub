package com.guesshub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipoDTO {

    private int id;
    private String nombre;
    private String pais;
    private int fundacion;
    private String logo;
    private String estadio;
    private String ciudad;

}