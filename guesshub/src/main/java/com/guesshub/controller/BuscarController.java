package com.guesshub.controller;

import com.guesshub.repository.CancionRepository;
import com.guesshub.repository.PeliculaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BuscarController {

    private final PeliculaRepository peliculaRepository;
    private final CancionRepository cancionRepository;
    private final HttpClient httpClient;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Value("${tmdb.api.url}")
    private String tmdbApiUrl;

    @Value("${apisports.api.key}")
    private String apisportsApiKey;

    @Value("${apisports.api.url}")
    private String apisportsApiUrl;

    private static final List<String> EQUIPOS_POPULARES = List.of(
            "Real Madrid", "Barcelona", "Boca Juniors", "River Plate",
            "Manchester City", "Liverpool", "Manchester United", "Arsenal",
            "Chelsea", "Bayern Munich", "Borussia Dortmund", "Juventus",
            "Inter", "Milan", "Paris Saint Germain", "Atletico Madrid",
            "Racing Club", "Independiente", "San Lorenzo", "Flamengo",
            "Palmeiras", "Tottenham", "Napoli", "Roma", "Aston Villa",
            "Benfica", "Porto", "Ajax", "Sporting CP", "Sevilla"
    );

    @GetMapping("/buscar")
    public ResponseEntity<List<String>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "pelicula") String modo) {

        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        String query = q.trim();
        List<String> resultados;

        try {
            resultados = switch (modo.toLowerCase()) {
                case "pelicula", "peliculas" -> buscarPeliculas(query);
                case "cancion", "canciones", "musica" -> buscarCanciones(query);
                case "equipo", "equipos", "futbol", "deportes" -> buscarEquipos(query);
                default -> buscarPeliculas(query);
            };
        } catch (Exception e) {
            log.warn("Error buscando sugerencias para '{}' (modo: {}): {}", query, modo, e.getMessage());
            resultados = List.of();
        }

        return ResponseEntity.ok(resultados);
    }

    private List<String> buscarPeliculas(String query) {
        Set<String> titulos = new LinkedHashSet<>();

        // 1. Buscar en TMDB live (búsqueda instantánea en vivo)
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = tmdbApiUrl + "/search/movie?api_key=" + tmdbApiKey
                    + "&query=" + encoded + "&language=es-AR&page=1";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(1200))
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                JSONObject json = new JSONObject(res.body());
                JSONArray arr = json.optJSONArray("results");
                if (arr != null) {
                    for (int i = 0; i < arr.length() && titulos.size() < 8; i++) {
                        String t = arr.getJSONObject(i).optString("title", "").trim();
                        if (!t.isEmpty()) titulos.add(t);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("TMDB search live falló: {}", e.getMessage());
        }

        // 2. Si no hay suficientes, complementar con la BD local
        if (titulos.size() < 8) {
            try {
                peliculaRepository.searchByTitulo(query)
                        .forEach(p -> titulos.add(p.getTitulo()));
            } catch (Exception ignored) {}
        }

        return new ArrayList<>(titulos);
    }

    private List<String> buscarCanciones(String query) {
        Set<String> resultados = new LinkedHashSet<>();

        // 1. Buscar en Deezer live (rápido, devuelve título y artista)
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://api.deezer.com/search?q=" + encoded + "&limit=8";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(1200))
                    .GET()
                    .build();

            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200) {
                JSONObject json = new JSONObject(res.body());
                JSONArray arr = json.optJSONArray("data");
                if (arr != null) {
                    for (int i = 0; i < arr.length() && resultados.size() < 8; i++) {
                        JSONObject item = arr.getJSONObject(i);
                        String title = item.optString("title_short", item.optString("title", "")).trim();
                        String artist = item.optJSONObject("artist") != null
                                ? item.getJSONObject("artist").optString("name", "").trim()
                                : "";
                        if (!title.isEmpty()) {
                            resultados.add(artist.isEmpty() ? title : title + " — " + artist);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Deezer search live falló: {}", e.getMessage());
        }

        // 2. Si no hay suficientes, complementar con la BD local
        if (resultados.size() < 8) {
            try {
                cancionRepository.searchByNombre(query)
                        .forEach(c -> resultados.add(c.getNombre() + " — " + c.getArtista()));
            } catch (Exception ignored) {}
        }

        return new ArrayList<>(resultados);
    }

    private List<String> buscarEquipos(String query) {
        Set<String> equipos = new LinkedHashSet<>();
        String qLower = query.toLowerCase();

        // 1. Coincidencias rápidas con lista local de equipos populares
        for (String eq : EQUIPOS_POPULARES) {
            if (eq.toLowerCase().contains(qLower)) {
                equipos.add(eq);
                if (equipos.size() >= 8) break;
            }
        }

        // 2. Si hay espacio y query tiene mín. 3 letras, buscar en API-Sports
        if (equipos.size() < 6 && query.length() >= 3) {
            try {
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = apisportsApiUrl + "/teams?search=" + encoded;

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("x-apisports-key", apisportsApiKey)
                        .timeout(Duration.ofMillis(1200))
                        .GET()
                        .build();

                HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 200) {
                    JSONObject json = new JSONObject(res.body());
                    JSONArray arr = json.optJSONArray("response");
                    if (arr != null) {
                        for (int i = 0; i < arr.length() && equipos.size() < 8; i++) {
                            JSONObject teamObj = arr.getJSONObject(i).optJSONObject("team");
                            if (teamObj != null) {
                                String name = teamObj.optString("name", "").trim();
                                if (!name.isEmpty()) equipos.add(name);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("API Sports team search falló: {}", e.getMessage());
            }
        }

        return new ArrayList<>(equipos);
    }
}