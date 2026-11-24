package com.guesshub.controller;

import com.guesshub.dto.InsigniaDTO;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.security.UsuarioRepository;
import com.guesshub.service.InsigniaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InsigniaController {

    private final InsigniaService insigniaService;
    private final UsuarioRepository usuarioRepository;

    /**
     * GET /api/insignias
     * Lista todas las insignias del sistema, marcando cuáles tiene desbloqueadas y cuál tiene equipada el usuario.
     */
    @GetMapping("/insignias")
    public ResponseEntity<List<InsigniaDTO>> getInsignias() {
        Usuario usuario = getUsuarioAutenticado().orElse(null);
        return ResponseEntity.ok(insigniaService.listarParaUsuario(usuario));
    }

    /**
     * PUT /api/usuario/insignia-equipada/{id}
     * Equipa una insignia en el perfil del usuario. Si id es 0 o negativo, desequipa.
     */
    @PutMapping("/usuario/insignia-equipada/{id}")
    public ResponseEntity<?> equiparInsignia(@PathVariable("id") Long id) {
        Optional<Usuario> userOpt = getUsuarioAutenticado();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        }

        try {
            Long insigniaId = (id != null && id > 0) ? id : null;
            insigniaService.equiparInsignia(userOpt.get(), insigniaId);
            return ResponseEntity.ok(Map.of("mensaje", "Insignia actualizada con éxito"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Optional<Usuario> getUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.equals("anonymousUser")) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsername(username);
    }
}

