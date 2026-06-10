package com.guesshub.controller.security;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guesshub.dto.security.LoginDTO;
import com.guesshub.repository.security.UsuarioRepository;
import com.guesshub.service.security.JwtTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        List<String> roles = userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        String token = jwtTokenService.generarToken(userDetails, roles);

        // Obtener displayName desde la BD para devolverlo al frontend
        String displayName = usuarioRepository.findByUsername(request.username())
                .map(u -> u.getDisplayName())
                .orElse(request.username().split("@")[0]);

        return ResponseEntity.ok(Map.of("token", token, "displayName", displayName));
    }
}