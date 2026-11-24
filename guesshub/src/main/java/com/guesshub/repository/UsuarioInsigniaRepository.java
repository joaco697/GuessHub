package com.guesshub.repository;

import com.guesshub.entity.UsuarioInsignia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioInsigniaRepository extends JpaRepository<UsuarioInsignia, Long> {

    List<UsuarioInsignia> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndInsigniaCodigo(Long usuarioId, String codigo);

    @Query("SELECT ui.insignia.codigo FROM UsuarioInsignia ui WHERE ui.usuario.id = :usuarioId")
    List<String> findCodigosByUsuarioId(@Param("usuarioId") Long usuarioId);
}

