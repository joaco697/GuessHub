package com.guesshub.entity;

import com.guesshub.entity.security.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_insignias",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "insignia_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioInsignia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insignia_id", nullable = false)
    private Insignia insignia;

    @Column(name = "fecha_obtencion", nullable = false)
    private LocalDateTime fechaObtencion = LocalDateTime.now();

    public UsuarioInsignia(Usuario usuario, Insignia insignia) {
        this.usuario = usuario;
        this.insignia = insignia;
        this.fechaObtencion = LocalDateTime.now();
    }
}

