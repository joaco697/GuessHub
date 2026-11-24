package com.guesshub.service.security;

import com.guesshub.dto.security.RestablecerPasswordDTO;
import com.guesshub.entity.security.PasswordResetToken;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.security.PasswordResetTokenRepository;
import com.guesshub.repository.security.UsuarioRepository;
import com.guesshub.service.NotificacionEmailService;
import com.guesshub.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacionEmailService notificacionEmailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public java.util.Optional<String> solicitarRecuperacion(String email) {
        if (email == null || email.trim().isEmpty()) {
            return java.util.Optional.empty();
        }

        var optUsuario = usuarioRepository.findByUsername(email.trim().toLowerCase());
        if (optUsuario.isEmpty()) {
            log.warn("Solicitud de recuperación para email no registrado: {}", email);
            return java.util.Optional.empty();
        }

        Usuario usuario = optUsuario.get();
        // Generar código numérico de 6 dígitos
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken(codigo, usuario, expiracion);
        tokenRepository.save(resetToken);

        notificacionEmailService.notificarRecuperacionPassword(
                usuario.getUsername(), usuario.getDisplayName(), codigo);

        log.info("Código de recuperación generado para usuario: {}", usuario.getUsername());
        return java.util.Optional.of(codigo);
    }

    @Transactional
    public void restablecerPassword(RestablecerPasswordDTO dto) {
        if (!dto.nuevaPassword().equals(dto.confirmacionPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        PasswordValidator.validate(dto.nuevaPassword());

        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsadoFalse(dto.token().trim())
                .orElseThrow(() -> new IllegalArgumentException("El código de recuperación es inválido o ya fue utilizado"));

        if (resetToken.estaExpirado()) {
            throw new IllegalArgumentException("El código de recuperación ha expirado. Por favor, solicita uno nuevo.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(dto.nuevaPassword()));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        log.info("Contraseña restablecida exitosamente para el usuario: {}", usuario.getUsername());
    }
}
