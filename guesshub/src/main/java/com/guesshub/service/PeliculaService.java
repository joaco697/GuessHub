package com.guesshub.service;

import com.guesshub.dto.PeliculaDTO;
import com.guesshub.service.TmdbService.PeliculaTmdb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeliculaService {

    private final TmdbService tmdbService;
    private final PreguntaActivaService preguntaActivaService;

    // Máximo de intentos para evitar loop infinito si casi todas las películas fallan en OMDB
    private static final int MAX_INTENTOS = 5;

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    @Value("${omdb.api.url}")
    private String omdbApiUrl;

    /**
     * Obtiene una película aleatoria (en vivo desde TMDB, sin tocar la base)
     * con todas las pistas válidas (desde OMDB), y registra la respuesta
     * correcta del lado del servidor — el título nunca viaja al cliente.
     */
    public PeliculaDTO obtenerPeliculaRandom() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        Set<String> yaIntentadas = new HashSet<>();

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            PeliculaTmdb pelicula = tmdbService.obtenerAleatoriaLive();

            if (yaIntentadas.contains(pelicula.tituloOriginal())) {
                log.debug("Intento {}: película {} ya probada, pidiendo otra", intento, pelicula.tituloOriginal());
                continue;
            }
            yaIntentadas.add(pelicula.tituloOriginal());

            PeliculaDTO dto = buscarEnOmdb(pelicula, client);

            if (tieneTodasLasPistas(dto)) {
                String preguntaId = preguntaActivaService.registrar(pelicula.titulo());
                dto.setId(preguntaId);
                log.info("Película encontrada con todas las pistas en intento {}", intento);
                return dto;
            }

            log.warn("Intento {}/{}: pistas incompletas para '{}', buscando otra película",
                    intento, MAX_INTENTOS, pelicula.tituloOriginal());
        }

        throw new RuntimeException(
                "No se encontró una película con pistas completas tras " + MAX_INTENTOS + " intentos. " +
                "Verificá que OMDB tenga datos para las películas de TMDB.");
    }

    /**
     * Consulta OMDB para una película y arma el DTO con pistas (sin título).
     */
    private PeliculaDTO buscarEnOmdb(PeliculaTmdb pelicula, HttpClient client) throws Exception {
        String query = URLEncoder.encode(pelicula.tituloOriginal(), StandardCharsets.UTF_8);
        String url = omdbApiUrl + "?apikey=" + omdbApiKey + "&t=" + query + "&type=movie";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());

        PeliculaDTO dto = new PeliculaDTO();

        if (!"True".equals(json.optString("Response"))) {
            log.warn("OMDB no encontró: '{}'", pelicula.tituloOriginal());
            dto.setPista1("");
            dto.setPista2("");
            dto.setPista3("");
            dto.setPoster("");
            dto.setSinopsis("");
            return dto;
        }

        String genero   = json.optString("Genre",    "").trim();
        String director = json.optString("Director", "").trim();
        String anio     = json.optString("Year",     "").trim();

        dto.setPista1(genero.isEmpty()   ? "" : "Género: "   + genero);
        dto.setPista2(director.isEmpty() ? "" : "Director: " + director);
        dto.setPista3(anio.isEmpty()     ? "" : "Año: "      + anio);
        dto.setPoster(json.optString("Poster", ""));
        dto.setSinopsis(json.optString("Plot", ""));

        return dto;
    }

    /**
     * Devuelve true solo si las tres pistas tienen contenido útil.
     */
    private boolean tieneTodasLasPistas(PeliculaDTO dto) {
        return dto.getPista1() != null && !dto.getPista1().isBlank()
            && dto.getPista2() != null && !dto.getPista2().isBlank()
            && dto.getPista3() != null && !dto.getPista3().isBlank();
    }
}
