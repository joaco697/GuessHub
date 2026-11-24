package com.guesshub.service;

import com.guesshub.entity.Insignia;
import com.guesshub.entity.Puntaje;
import com.guesshub.entity.UsuarioInsignia;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.InsigniaRepository;
import com.guesshub.repository.UsuarioInsigniaRepository;
import com.guesshub.repository.security.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsigniaServiceTest {

    @Mock
    private InsigniaRepository insigniaRepository;
    @Mock
    private UsuarioInsigniaRepository usuarioInsigniaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private InsigniaService insigniaService;

    private Usuario usuario;
    private Puntaje puntaje;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("jugador@test.com", "pass123", "JugadorTest");
        usuario.setId(1L);
        puntaje = new Puntaje(usuario);
    }

    @Test
    @DisplayName("Desbloqueo de insignia por 3 días de racha global")
    void testDesbloqueoRacha3Dias() {
        puntaje.setRachaDias(3);
        Insignia ins = new Insignia("RACHA_3", "Constante", "3 días seguidos", "🔥", "RACHA");

        when(usuarioInsigniaRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(insigniaRepository.findByCodigo("RACHA_3")).thenReturn(Optional.of(ins));

        List<String> nuevas = insigniaService.evaluarInsignias(usuario, puntaje, 1, 50);

        assertTrue(nuevas.contains("Constante"));
        verify(usuarioInsigniaRepository, times(1)).save(any(UsuarioInsignia.class));
    }

    @Test
    @DisplayName("Desbloqueo de insignia Cinéfilo al superar 1000 puntos en Películas")
    void testDesbloqueoCinefilo() {
        puntaje.setPuntosPeliculas(1200);
        Insignia ins = new Insignia("CINEFILO", "Cinéfilo", "1000+ en Películas", "🎬", "CATEGORIA");

        when(usuarioInsigniaRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(insigniaRepository.findByCodigo("CINEFILO")).thenReturn(Optional.of(ins));

        List<String> nuevas = insigniaService.evaluarInsignias(usuario, puntaje, 0, 50);

        assertTrue(nuevas.contains("Cinéfilo"));
    }

    @Test
    @DisplayName("Desbloqueo de insignia Top 10 al posicionarse entre los primeros 10")
    void testDesbloqueoTop10() {
        Insignia ins = new Insignia("TOP_10", "Entre los Mejores", "Top 10 ranking", "🏆", "RANKING");

        when(usuarioInsigniaRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());
        when(insigniaRepository.findByCodigo("TOP_10")).thenReturn(Optional.of(ins));

        List<String> nuevas = insigniaService.evaluarInsignias(usuario, puntaje, 0, 3);

        assertTrue(nuevas.contains("Entre los Mejores"));
    }

    @Test
    @DisplayName("No se puede equipar una insignia que el usuario no ha desbloqueado")
    void testEquiparInsigniaNoPoseida() {
        Insignia ins = new Insignia("RACHA_30", "Leyenda", "30 días", "👑", "RACHA");
        ins.setId(99L);

        when(insigniaRepository.findById(99L)).thenReturn(Optional.of(ins));
        when(usuarioInsigniaRepository.findByUsuarioId(1L)).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> insigniaService.equiparInsignia(usuario, 99L));
    }

    @Test
    @DisplayName("Equipar insignia exitosamente cuando el usuario sí la posee")
    void testEquiparInsigniaPoseida() {
        Insignia ins = new Insignia("CINEFILO", "Cinéfilo", "1000+", "🎬", "CATEGORIA");
        ins.setId(10L);
        UsuarioInsignia ui = new UsuarioInsignia(usuario, ins);

        when(insigniaRepository.findById(10L)).thenReturn(Optional.of(ins));
        when(usuarioInsigniaRepository.findByUsuarioId(1L)).thenReturn(List.of(ui));

        insigniaService.equiparInsignia(usuario, 10L);

        assertEquals(ins, usuario.getInsigniaEquipada());
        verify(usuarioRepository, times(1)).save(usuario);
    }
}
