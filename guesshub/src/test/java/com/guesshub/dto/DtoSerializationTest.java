package com.guesshub.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cancionDtoDebeIncluirCampoIdEnJson() throws Exception {
        CancionDTO cancion = new CancionDTO();
        cancion.setId("uuid-cancion-123");
        cancion.setPista1("Artista Test");
        cancion.setPista2("Album Test");
        cancion.setPista3("2024");
        cancion.setLogo("https://example.com/cover.jpg");
        cancion.setPreview("https://example.com/preview.mp3");

        String json = objectMapper.writeValueAsString(cancion);

        assertTrue(json.contains("\"id\":\"uuid-cancion-123\""),
                "El JSON de CancionDTO debe incluir el campo 'id' con su valor UUID");
    }

    @Test
    void peliculaDtoDebeIncluirCampoIdEnJson() throws Exception {
        PeliculaDTO pelicula = new PeliculaDTO();
        pelicula.setId("uuid-pelicula-456");
        pelicula.setPista1("Genero: Accion");
        pelicula.setPista2("Director: Nolan");
        pelicula.setPista3("Año: 2010");
        pelicula.setPoster("https://example.com/poster.jpg");
        pelicula.setSinopsis("Sinopsis prueba");

        String json = objectMapper.writeValueAsString(pelicula);

        assertTrue(json.contains("\"id\":\"uuid-pelicula-456\""),
                "El JSON de PeliculaDTO debe incluir el campo 'id' con su valor UUID");
    }

    @Test
    void equipoDtoDebeIncluirCampoIdEnJson() throws Exception {
        EquipoDTO equipo = new EquipoDTO();
        equipo.setId("uuid-equipo-789");
        equipo.setPais("Argentina");
        equipo.setFundacion(1905);
        equipo.setEstadio("La Bombonera");

        String json = objectMapper.writeValueAsString(equipo);

        assertTrue(json.contains("\"id\":\"uuid-equipo-789\""),
                "El JSON de EquipoDTO debe incluir el campo 'id' con su valor UUID");
    }

    @Test
    void respuestaDtoAceptaRespuestaVaciaYAliasDeId() throws Exception {
        String jsonConPreguntaId = "{\"preguntaId\":\"uuid-1\",\"respuesta\":\"\"}";
        RespuestaDTO dto1 = objectMapper.readValue(jsonConPreguntaId, RespuestaDTO.class);
        assertEquals("uuid-1", dto1.preguntaId());
        assertEquals("", dto1.respuesta());

        String jsonConPeliculaId = "{\"peliculaId\":\"uuid-legacy\",\"respuesta\":\"Inception\"}";
        RespuestaDTO dto2 = objectMapper.readValue(jsonConPeliculaId, RespuestaDTO.class);
        assertEquals("uuid-legacy", dto2.preguntaId());
        assertEquals("Inception", dto2.respuesta());
    }
}
