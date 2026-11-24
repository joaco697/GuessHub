package com.guesshub.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guesshub.repository.security.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Páginas HTML — acceso libre (las no existentes van a /error en vez de 401)
                        .requestMatchers(HttpMethod.GET, "/", "/game", "/ranking", "/perfil", "/index", "/admin").permitAll()
                        .requestMatchers(HttpMethod.GET, "/error").permitAll()
                        // Recursos estáticos
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // Auth pública
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/registrar").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")

                        // Admin: gestión, sincronización y monitoreo live
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Categorías de juego en vivo, desafío diario y respuestas
                        .requestMatchers(HttpMethod.GET, "/api/random", "/api/cancion/random").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pelicula/random").permitAll()
                        .requestMatchers(HttpMethod.GET, "/equipo/random", "/api/futbol/random", "/api/deportes/random").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/desafio-diario").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/respuesta").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/partida").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ranking").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/insignias").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/feedback").permitAll()

                        // Endpoints de fútbol/deportes (públicos: random; resto protegido)
                        .requestMatchers(HttpMethod.POST, "/equipo").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/equipo").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/buscar").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Rutas no protegidas de tipo página web HTML van al manejador de errores
                        .requestMatchers(HttpMethod.GET, "/*").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint()))
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), java.util.Map.of(
                    "type", "https://http.dev/problems/access-denied",
                    "title", "Acceso denegado",
                    "status", 403,
                    "detail", "No tenés permisos para acceder a este recurso"));
        };
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), java.util.Map.of(
                    "type", "https://http.dev/problems/unauthorized",
                    "title", "No autorizado",
                    "status", 401,
                    "detail", "Credenciales inválidas o faltantes"));
        };
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> usuarioRepository.findByUsername(username)
                .map(usuario -> org.springframework.security.core.userdetails.User
                        .withUsername(usuario.getUsername())
                        .password(usuario.getPassword())
                        .roles(usuario.getRoles().stream()
                                .map(r -> r.getNombre())
                                .toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
