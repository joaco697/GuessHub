package com.guesshub.controller;

import com.guesshub.dto.FeedbackDTO;
import com.guesshub.entity.Feedback;
import com.guesshub.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * POST /api/feedback
     * Registra feedback guiado o sugerencias de nuevos minijuegos.
     */
    @PostMapping("/feedback")
    public ResponseEntity<?> enviarFeedback(@Valid @RequestBody FeedbackDTO dto) {
        try {
            Feedback feedback = feedbackService.guardarFeedback(dto);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "¡Muchas gracias por tu feedback!",
                    "id", feedback.getId()
            ));
        } catch (Exception e) {
            log.error("Error al guardar feedback: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "No se pudo registrar el feedback"));
        }
    }

    /**
     * GET /api/admin/feedback
     * Consulta feedbacks para administración.
     */
    @GetMapping("/admin/feedback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Feedback>> listarFeedbacks() {
        return ResponseEntity.ok(feedbackService.listarTodos());
    }
}

