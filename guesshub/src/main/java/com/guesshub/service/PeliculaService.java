package com.guesshub.service;

import com.guesshub.dto.PeliculaDTO;
import com.guesshub.dto.ResultadoDTO;
import com.guesshub.entity.Pelicula;
import com.guesshub.repository.PeliculaRepository;
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
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeliculaService {

    private final PeliculaRepository peliculaRepository;

    // Máximo de intentos para evitar loop infinito si casi todas las películas fallan en OMDB
    private static final int MAX_INTENTOS = 5;

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    @Value("${omdb.api.url}")
    private String omdbApiUrl;

    /**
     * Obtiene una película aleatoria con todas las pistas válidas.
     * Si alguna pista queda como "desconocido", reintenta con otra película.
     * Tras MAX_INTENTOS fallidos lanza una excepción.
     */
    public PeliculaDTO obtenerPeliculaRandom() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        Set<Long> yaIntentadas = new HashSet<>();

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            Pelicula pelicula = peliculaRepository.findRandom()
                    .orElseThrow(() -> new RuntimeException(
                            "No hay películas en la base de datos. Hacé sync primero: POST /api/admin/sync-peliculas"));

            // Evitar reintentar la misma película
            if (yaIntentadas.contains(pelicula.getId())) {
                log.debug("Intento {}: película {} ya probada, pidiendo otra", intento, pelicula.getId());
                continue;
            }
            yaIntentadas.add(pelicula.getId());

            PeliculaDTO dto = buscarEnOmdb(pelicula, client);

            if (tieneTodasLasPistas(dto)) {
                log.info("Película encontrada con todas las pistas en intento {}: {}", intento, pelicula.getTitulo());
                return dto;
            }

            log.warn("Intento {}/{}: pistas incompletas para '{}', buscando otra película",
                    intento, MAX_INTENTOS, pelicula.getTituloOriginal());
        }

        throw new RuntimeException(
                "No se encontró una película con pistas completas tras " + MAX_INTENTOS + " intentos. " +
                "Verificá que OMDB tenga datos para las películas sincronizadas.");
    }

    /**
     * Consulta OMDB para una película y arma el DTO con pistas.
     */
    private PeliculaDTO buscarEnOmdb(Pelicula pelicula, HttpClient client) throws Exception {
        String query = URLEncoder.encode(pelicula.getTituloOriginal(), StandardCharsets.UTF_8);
        String url = omdbApiUrl + "?apikey=" + omdbApiKey + "&t=" + query + "&type=movie";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());

        PeliculaDTO dto = new PeliculaDTO();
        dto.setId(pelicula.getId());
        dto.setTitulo(pelicula.getTitulo());

        if (!"True".equals(json.optString("Response"))) {
            log.warn("OMDB no encontró: '{}'", pelicula.getTituloOriginal());
            // Devolvemos DTO con pistas vacías para que el llamador decida reintentar
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

    /**
     * Valida si la respuesta del usuario coincide con el título de la película.
     * Comparación flexible: ignora mayúsculas, tildes y espacios extra.
     */
    public ResultadoDTO validarRespuesta(Long peliculaId, String respuesta) {
        Pelicula pelicula = peliculaRepository.findById(peliculaId)
                .orElseThrow(() -> new RuntimeException("Película no encontrada"));

        String tituloNormalizado    = normalizar(pelicula.getTitulo());
        String respuestaNormalizada = normalizar(respuesta);

        boolean correcto = tituloNormalizado.equals(respuestaNormalizada)
                || normalizar(pelicula.getTituloOriginal()).equals(respuestaNormalizada);

        return new ResultadoDTO(
                correcto,
                pelicula.getTitulo(),
                correcto ? "¡Correcto!" : "Incorrecto, la respuesta era: " + pelicula.getTitulo()
        );
    }

    /**
     * Normaliza un string: minúsculas, sin tildes, sin espacios extra.
     */
    private String normalizar(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinTildes.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}