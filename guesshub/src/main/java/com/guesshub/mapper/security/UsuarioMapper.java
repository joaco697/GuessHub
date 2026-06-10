package com.guesshub.mapper.security;

import java.util.List;

import org.springframework.stereotype.Component;

import com.guesshub.dto.security.RegistrarDTO;
import com.guesshub.dto.security.UsuarioDTO;
import com.guesshub.entity.security.Rol;
import com.guesshub.entity.security.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;
        return new UsuarioDTO(
            usuario.getUsername(),
            usuario.getDisplayName(),
            usuario.getRoles().stream().map(Rol::getNombre).toList()
        );
    }

    public List<UsuarioDTO> toDTOList(List<Usuario> usuarios) {
        return usuarios.stream().map(this::toDTO).toList();
    }

    public Usuario toEntity(RegistrarDTO dto) {
        if (dto == null) return null;
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.username());
        usuario.setDisplayName(dto.displayName());
        return usuario;
    }
}