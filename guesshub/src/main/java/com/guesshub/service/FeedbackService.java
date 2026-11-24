package com.guesshub.service;

import com.guesshub.dto.FeedbackDTO;
import com.guesshub.entity.Feedback;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.FeedbackRepository;
import com.guesshub.repository.security.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Feedback guardarFeedback(FeedbackDTO dto) {
        Usuario usuario = null;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.equals("anonymousUser")) {
            Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                usuario = userOpt.get();
            }
        }

        Feedback feedback = new Feedback(
                usuario,
                dto.emailContacto(),
                dto.tipo(),
                dto.opcionGuiada(),
                dto.comentario()
        );

        Feedback guardado = feedbackRepository.save(feedback);
        log.info("Feedback registrado exitosamente: tipo={}, usuario={}", dto.tipo(), username);
        return guardado;
    }

    @Transactional(readOnly = true)
    public List<Feedback> listarTodos() {
        return feedbackRepository.findAllByOrderByFechaDesc();
    }
}

