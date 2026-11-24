package com.guesshub.service.security;

import com.guesshub.dto.security.RestablecerPasswordDTO;
import com.guesshub.entity.security.PasswordResetToken;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.security.PasswordResetTokenRepository;
import com.guesshub.repository.security.UsuarioRepository;
import com.guesshub.service.NotificacionEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificacionEmailService notificacionEmailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("olvidadizo@test.com", "passVieja123", "Olvidadizo");
        usuario.setId(10L);
    }

    @Test
    @DisplayName("Solicitud de recuperación para usuario existente genera token y envía correo")
    void testSolicitarRecuperacionExitoso() {
        when(usuarioRepository.findByUsername("olvidadizo@test.com")).thenReturn(Optional.of(usuario));

        Optional<String> codigo = passwordResetService.solicitarRecuperacion("olvidadizo@test.com");

        assertTrue(codigo.isPresent());
        assertEquals(6, codigo.get().length());
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(notificacionEmailService, times(1))
                .notificarRecuperacionPassword(eq("olvidadizo@test.com"), eq("Olvidadizo"), anyString());
    }

    @Test
    @DisplayName("Solicitud de recuperación para email inexistente no lanza error ni envía correo")
    void testSolicitarRecuperacionInexistente() {
        when(usuarioRepository.findByUsername("inexistente@test.com")).thenReturn(Optional.empty());

        Optional<String> codigo = passwordResetService.solicitarRecuperacion("inexistente@test.com");

        assertTrue(codigo.isEmpty());
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(notificacionEmailService, never()).notificarRecuperacionPassword(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Restablecer contraseña exitoso actualiza usuario y marca token como usado")
    void testRestablecerPasswordExitoso() {
        PasswordResetToken token = new PasswordResetToken("123456", usuario, LocalDateTime.now().plusMinutes(15));
        when(tokenRepository.findByTokenAndUsadoFalse("123456")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NuevaPassSegura@123")).thenReturn("encodedPassword");

        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("123456", "NuevaPassSegura@123", "NuevaPassSegura@123");

        passwordResetService.restablecerPassword(dto);

        assertTrue(token.isUsado());
        assertEquals("encodedPassword", usuario.getPassword());
        verify(usuarioRepository, times(1)).save(usuario);
        verify(tokenRepository, times(1)).save(token);
    }

    @Test
    @DisplayName("Restablecer contraseña falla si las contraseñas no coinciden")
    void testRestablecerPasswordNoCoinciden() {
        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("123456", "PassUno123", "PassOtro123");

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.restablecerPassword(dto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Restablecer contraseña falla si la contraseña tiene menos de 8 caracteres")
    void testRestablecerPasswordCorta() {
        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("123456", "corta", "corta");

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.restablecerPassword(dto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Restablecer contraseña falla si el token expiró")
    void testRestablecerPasswordExpirado() {
        PasswordResetToken tokenExpirado = new PasswordResetToken("123456", usuario, LocalDateTime.now().minusMinutes(5));
        when(tokenRepository.findByTokenAndUsadoFalse("123456")).thenReturn(Optional.of(tokenExpirado));

        RestablecerPasswordDTO dto = new RestablecerPasswordDTO("123456", "NuevaPassSegura@123", "NuevaPassSegura@123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.restablecerPassword(dto));

        assertTrue(ex.getMessage().contains("expirado"));
        verify(usuarioRepository, never()).save(any());
    }
}
