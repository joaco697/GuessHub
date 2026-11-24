package com.guesshub.service;

import com.guesshub.dto.FeedbackDTO;
import com.guesshub.entity.Feedback;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.FeedbackRepository;
import com.guesshub.repository.security.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Guardar feedback guiado anónimo exitosamente")
    void testGuardarFeedbackAnonimo() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("anonymousUser");

        FeedbackDTO dto = new FeedbackDTO("anon@test.com", "SUGERENCIA_MINIJUEGO", "Muy divertida", "Sugerencia test");
        Feedback feedbackGuardado = new Feedback(null, dto.emailContacto(), dto.tipo(), dto.opcionGuiada(), dto.comentario());
        feedbackGuardado.setId(1L);

        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedbackGuardado);

        Feedback resultado = feedbackService.guardarFeedback(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertNull(resultado.getUsuario());
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    @DisplayName("Guardar feedback guiado con usuario autenticado asociado")
    void testGuardarFeedbackAutenticado() {
        Usuario usuario = new Usuario("user@test.com", "pass123", "UserTest");
        usuario.setId(5L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@test.com");
        when(usuarioRepository.findByUsername("user@test.com")).thenReturn(Optional.of(usuario));

        FeedbackDTO dto = new FeedbackDTO(null, "DIFICULTAD", "Muy difícil", "Pistas complejas");
        Feedback feedbackGuardado = new Feedback(usuario, null, dto.tipo(), dto.opcionGuiada(), dto.comentario());
        feedbackGuardado.setId(2L);

        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedbackGuardado);

        Feedback resultado = feedbackService.guardarFeedback(dto);

        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
    }
}
