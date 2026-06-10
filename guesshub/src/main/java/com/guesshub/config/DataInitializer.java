package com.guesshub.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.guesshub.entity.security.Rol;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.security.RolRepository;
import com.guesshub.repository.security.UsuarioRepository;
import com.guesshub.util.PasswordValidator;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder encoder) {
        return args -> {
            Rol admin   = rolRepository.findByNombre("ADMIN").orElseGet(() -> rolRepository.save(new Rol("ADMIN")));
            Rol cliente = rolRepository.findByNombre("CLIENTE").orElseGet(() -> rolRepository.save(new Rol("CLIENTE")));

            if (usuarioRepository.findByUsername("admin@nextmatch.scaputo.edu.ar").isEmpty()) {
                String adminPassword = "AdminSuperSegura@123";
                PasswordValidator.validate(adminPassword);
                Usuario u = new Usuario("admin@nextmatch.scaputo.edu.ar", encoder.encode(adminPassword), "Admin");
                u.setRoles(Set.of(admin));
                usuarioRepository.save(u);
            }

            if (usuarioRepository.findByUsername("cliente@nextmatch.sbigliardi.edu.ar").isEmpty()) {
                String clientePassword = "ClienteSuperSegura@123";
                PasswordValidator.validate(clientePassword);
                Usuario u = new Usuario("cliente@nextmatch.sbigliardi.edu.ar", encoder.encode(clientePassword), "Bigliardi");
                u.setRoles(Set.of(cliente));
                usuarioRepository.save(u);
            }
        };
    }
}