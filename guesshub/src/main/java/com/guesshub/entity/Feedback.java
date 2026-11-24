package com.guesshub.entity;

import com.guesshub.entity.security.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    // SUGERENCIA_JUEGO, DIFICULTAD, EXPERIENCIA, BUG, OTRO
    @Column(nullable = false, length = 50)
    private String tipo;

    // Opción seleccionada del cuestionario cerrado guiado
    @Column(name = "opcion_guiada", nullable = false, length = 100)
    private String opcionGuiada;

    // Comentario o sugerencia opcional del usuario
    @Column(length = 1000)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Feedback(Usuario usuario, String emailContacto, String tipo, String opcionGuiada, String comentario) {
        this.usuario = usuario;
        this.emailContacto = emailContacto;
        this.tipo = tipo;
        this.opcionGuiada = opcionGuiada;
        this.comentario = comentario;
        this.fecha = LocalDateTime.now();
    }
}

