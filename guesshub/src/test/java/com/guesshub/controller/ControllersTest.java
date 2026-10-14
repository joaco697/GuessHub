package com.guesshub.controller;

import com.guesshub.dto.CancionDTO;
import com.guesshub.dto.EquipoDTO;
import com.guesshub.service.CancionService;
import com.guesshub.service.FutbolService;
import com.guesshub.service.LastFmService;
import com.guesshub.service.TmdbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllersTest {

    @Mock
    private CancionService cancionService;

    @Mock
    private FutbolService futbolService;

    @Mock
    private LastFmService lastFmService;

    @Mock
    private TmdbService tmdbService;

    @InjectMocks
    private CancionController cancionController;

    @InjectMocks
    private FutbolController futbolController;

    @InjectMocks
    private AdminController adminController;

    @Test
    void cancionControllerDevuelveListaCanciones() throws Exception {
        CancionDTO dto = new CancionDTO();
        dto.setId("c-1");
        when(cancionService.obtenerTrackRandom()).thenReturn(List.of(dto));

        ResponseEntity<List<CancionDTO>> response = cancionController.obtenerCancionRandom();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("c-1", response.getBody().get(0).getId());
    }

    @Test
    void futbolControllerDevuelveListaEquipos() {
        EquipoDTO dto = new EquipoDTO();
        dto.setId("e-1");
        when(futbolService.obtenerEquipoRandom()).thenReturn(List.of(dto));

        ResponseEntity<List<EquipoDTO>> response = futbolController.obtenerEquipoRandom();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("e-1", response.getBody().get(0).getId());
    }

    @Test
    void adminControllerDevuelveStatusLive() {
        ResponseEntity<Map<String, Object>> response = adminController.estadoSistema();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("LIVE_API", response.getBody().get("modoJuego"));
    }
}
