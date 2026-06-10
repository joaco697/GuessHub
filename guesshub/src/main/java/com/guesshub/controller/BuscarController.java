package com.guesshub.controller;

import com.guesshub.repository.CancionRepository;
import com.guesshub.repository.PeliculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BuscarController {

    private final PeliculaRepository peliculaRepository;
    private final CancionRepository cancionRepository;

    @GetMapping("/buscar")
    public ResponseEntity<List<String>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "pelicula") String modo) {

        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<String> resultados;

        if ("pelicula".equals(modo)) {
            resultados = peliculaRepository.searchByTitulo(q.trim())
                    .stream()
                    .map(p -> p.getTitulo())
                    .toList();
        } else {
            resultados = cancionRepository.searchByNombre(q.trim())
                    .stream()
                    .map(c -> c.getNombre() + " — " + c.getArtista())
                    .toList();
        }

        return ResponseEntity.ok(resultados);
    }
}