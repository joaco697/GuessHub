package com.guesshub.config;

import com.guesshub.entity.Puntaje;
import com.guesshub.entity.security.Rol;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.PuntajeRepository;
import com.guesshub.repository.security.RolRepository;
import com.guesshub.repository.security.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/**
 * Inicializador de datos para asegurar exactamente los perfiles requeridos:
 * 1. admin (admin@gmail.com / admin123) - Rol ADMIN y CLIENTE
 * 2. joaco (joacodeca07@gmail.com / joaco123) - Rol CLIENTE con puntuación en ranking
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PuntajeRepository puntajeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Verificando roles y perfiles base solicitados...");

        // 1. Roles
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseGet(() -> rolRepository.save(new Rol("CLIENTE")));

        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseGet(() -> rolRepository.save(new Rol("ADMIN")));

        // 2. Perfil 1: admin (admin@gmail.com / admin123)
        Usuario admin = usuarioRepository.findByUsername("admin@gmail.com").orElse(null);
        if (admin == null) {
            admin = new Usuario("admin@gmail.com", passwordEncoder.encode("admin123"), "admin");
            admin.setRoles(Set.of(rolAdmin, rolCliente));
            admin.setFotoPerfil("⚡");
            usuarioRepository.save(admin);
            log.info("Usuario admin creado exitosamente: admin@gmail.com");
        } else {
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDisplayName("admin");
            admin.setRoles(Set.of(rolAdmin, rolCliente));
            usuarioRepository.save(admin);
        }

        if (puntajeRepository.findByUsuarioId(admin.getId()).isEmpty()) {
            Puntaje pAdmin = new Puntaje(admin);
            pAdmin.setPuntos(1500);
            pAdmin.setPartidasJugadas(3);
            pAdmin.setPartidasCorrectas(3);
            pAdmin.setRachaDias(2);
            pAdmin.setUltimaPartidaFecha(LocalDate.now());
            pAdmin.setPuntosPeliculas(600);
            pAdmin.setPuntosMusica(500);
            pAdmin.setPuntosDeportes(400);
            puntajeRepository.save(pAdmin);
        }

        // 3. Perfil 2: joaco (joacodeca07@gmail.com / joaco123)
        Usuario joaco = usuarioRepository.findByUsername("joacodeca07@gmail.com").orElse(null);
        if (joaco == null) {
            joaco = new Usuario("joacodeca07@gmail.com", passwordEncoder.encode("joaco123"), "joaco");
            joaco.setRoles(Set.of(rolCliente));
            joaco.setFotoPerfil("🚀");
            usuarioRepository.save(joaco);
            log.info("Usuario joaco creado exitosamente: joacodeca07@gmail.com");
        } else {
            joaco.setPassword(passwordEncoder.encode("joaco123"));
            joaco.setDisplayName("joaco");
            joaco.setRoles(Set.of(rolCliente));
            usuarioRepository.save(joaco);
        }

        if (puntajeRepository.findByUsuarioId(joaco.getId()).isEmpty()) {
            Puntaje pJoaco = new Puntaje(joaco);
            pJoaco.setPuntos(2850);
            pJoaco.setPartidasJugadas(5);
            pJoaco.setPartidasCorrectas(4);
            pJoaco.setRachaDias(4);
            pJoaco.setUltimaPartidaFecha(LocalDate.now());
            pJoaco.setPuntosPeliculas(1100);
            pJoaco.setPuntosMusica(950);
            pJoaco.setPuntosDeportes(800);
            puntajeRepository.save(pJoaco);
        }

        log.info("Inicialización de perfiles admin y joaco completada.");
    }
}
