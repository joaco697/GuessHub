package com.guesshub.controller.security;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.guesshub.dto.security.RegistrarDTO;
import com.guesshub.dto.security.UsuarioDTO;
import com.guesshub.entity.security.Usuario;
import com.guesshub.mapper.security.UsuarioMapper;
import com.guesshub.repository.security.UsuarioRepository;
import com.guesshub.service.security.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioMapper usuarioMapper;

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioDTO> registrarCliente(@Valid @RequestBody RegistrarDTO registrarDTO) {
        Usuario usuario = usuarioMapper.toEntity(registrarDTO);
        Usuario nuevoUsuario = usuarioService.registrar(usuario, registrarDTO.password(), registrarDTO.verificacionPassword());
        UsuarioDTO nuevoUsuarioDTO = usuarioMapper.toDTO(nuevoUsuario);
        return ResponseEntity.ok(nuevoUsuarioDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .map(u -> ResponseEntity.ok(usuarioMapper.toDTO(u)))
                .orElse(ResponseEntity.status(401).build());
    }

    @PutMapping("/me/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .map(u -> {
                    if (body.containsKey("fotoPerfil") && body.get("fotoPerfil") != null) {
                        u.setFotoPerfil(body.get("fotoPerfil").trim());
                    }
                    if (body.containsKey("displayName") && body.get("displayName") != null) {
                        String dn = body.get("displayName").trim();
                        if (dn.length() >= 5 && dn.length() <= 15) {
                            u.setDisplayName(dn);
                        }
                    }
                    usuarioRepository.save(u);
                    return ResponseEntity.ok(usuarioMapper.toDTO(u));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioDTO> usuarioDTOs = usuarioMapper.toDTOList(usuarios);
        return ResponseEntity.ok(usuarioDTOs);
    }
}
