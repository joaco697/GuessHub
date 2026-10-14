package com.guesshub.service;

import com.guesshub.dto.EquipoDTO;
import com.guesshub.repository.EquipoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FutbolServiceTest {

    @Mock
    private PreguntaActivaService preguntaActivaService;

    @Mock
    private EquipoRepository equipoRepository;

    @InjectMocks
    private FutbolService futbolService;

    @Test
    void fallbackDevuelveEquipoValidoYRegistraPregunta() {
        when(equipoRepository.findAll()).thenReturn(Collections.emptyList());
        when(preguntaActivaService.registrar(anyString())).thenReturn("mock-uuid-123");

        List<EquipoDTO> resultado = futbolService.obtenerEquipoFallback();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        EquipoDTO dto = resultado.get(0);
        assertEquals("mock-uuid-123", dto.getId());
        assertNotNull(dto.getPais());
        assertNotNull(dto.getEstadio());
        assertTrue(dto.getFundacion() > 0);
        verify(preguntaActivaService).registrar(anyString());
    }
}
