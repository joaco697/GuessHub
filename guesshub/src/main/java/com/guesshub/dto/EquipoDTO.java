package com.guesshub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NUNCA incluye el nombre del equipo: la respuesta correcta vive solo en
 * el servidor (PreguntaActivaService), identificada por "id" (UUID).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipoDTO {
    private String id;
    private String pais;
    private int fundacion;
    private String logo;
    private String estadio;
    private String ciudad;
}
