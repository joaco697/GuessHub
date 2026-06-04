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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeliculaService {

    private final PeliculaRepository peliculaRepository;

    @Value("${omdb.api.key}")
    private String omdbApiKey;

    @Value("${omdb.api.url}")
    private String omdbApiUrl;

    /**
     * 1. Obtiene una película aleatoria de la BD
     * 2. La busca en OMDB para obtener director, género, año y poster
     * 3. Devuelve el DTO con pistas para el juego (incluye el id para luego validar)
     */
    public PeliculaDTO obtenerPeliculaRandom() throws Exception {

        Pelicula pelicula = peliculaRepository.findRandom()
                .orElseThrow(() -> new RuntimeException(
                        "No hay películas en la base de datos. Hacé sync primero: POST /api/admin/sync-peliculas"));

        String query = URLEncoder.encode(pelicula.getTituloOriginal(), StandardCharsets.UTF_8);
        String url = omdbApiUrl + "?apikey=" + omdbApiKey + "&t=" + query + "&type=movie";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());

        PeliculaDTO dto = new PeliculaDTO();
        dto.setId(pelicula.getId());
        dto.setTitulo(pelicula.getTitulo());

        if (!"True".equals(json.optString("Response"))) {
            log.warn("OMDB no encontró: {}", pelicula.getTituloOriginal());
            dto.setPista1("Género desconocido");
            dto.setPista2("Director desconocido");
            dto.setPista3("Año desconocido");
            dto.setPoster("");
            dto.setSinopsis("");
            return dto;
        }

        dto.setPista1("Género: " + json.optString("Genre", "Desconocido"));
        dto.setPista2("Director: " + json.optString("Director", "Desconocido"));
        dto.setPista3("Año: " + json.optString("Year", "Desconocido"));
        dto.setPoster(json.optString("Poster", ""));
        dto.setSinopsis(json.optString("Plot", ""));

        return dto;
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