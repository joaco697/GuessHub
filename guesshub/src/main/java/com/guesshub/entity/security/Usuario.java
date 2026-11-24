package com.guesshub.entity.security;

import java.util.HashSet;
import java.util.Set;

import com.guesshub.entity.Insignia;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @Email(message = "El nombre de usuario debe ser un email válido")
    @NotBlank(message = "El nombre de usuario es requerido")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña es requerida")
    private String password;

    // Nombre visible en ranking y perfil (5 a 15 caracteres)
    @Column(name = "display_name", nullable = false, unique = true, length = 15)
    private String displayName;

    // Foto de perfil o avatar seleccionado
    @Column(name = "foto_perfil", length = 255)
    private String fotoPerfil = "👤";

    // Insignia que el usuario decide lucir en su perfil / rankings
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insignia_equipada_id")
    private Insignia insigniaEquipada;

    // Comodines para congelar racha sin perderla
    @Column(name = "comodines_racha", columnDefinition = "integer default 1")
    private int comodinesRacha = 1;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
        this.displayName = username.contains("@") ? username.split("@")[0] : username;
        this.fotoPerfil = "👤";
        this.comodinesRacha = 1;
    }

    public Usuario(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.fotoPerfil = "👤";
        this.comodinesRacha = 1;
    }
}
