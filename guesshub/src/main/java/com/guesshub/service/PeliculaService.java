package com.guesshub.service;

import com.guesshub.dto.PeliculaDTO;
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
     * 1. Obtiene una película aleatoria de la BD (sincronizada desde TMDB)
     * 2. La busca en OMDB para obtener director, género, año y poster
     * 3. Devuelve el DTO con pistas para el juego
     */
    public List<PeliculaDTO> obtenerPeliculaRandom() throws Exception {

        Pelicula pelicula = peliculaRepository.findRandom()
                .orElseThrow(() -> new RuntimeException(
                        "No hay películas en la base de datos. Hacé sync primero: POST /api/admin/sync-peliculas"));

        // Buscar en OMDB por título original (mejor coincidencia)
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
        dto.setTitulo(pelicula.getTitulo());

        if (!"True".equals(json.optString("Response"))) {
            // OMDB no encontró nada, devolvemos con lo mínimo
            log.warn("OMDB no encontró: {}", pelicula.getTituloOriginal());
            dto.setPista1("Género desconocido");
            dto.setPista2("Director desconocido");
            dto.setPista3("Año desconocido");
            dto.setPoster("");
            dto.setSinopsis("");
            return List.of(dto);
        }

        // Pistas para el juego
        String genero   = json.optString("Genre",    "Género desconocido");
        String director = json.optString("Director", "Director desconocido");
        String anio     = json.optString("Year",     "Año desconocido");
        String poster   = json.optString("Poster",   "");
        String sinopsis = json.optString("Plot",     "");

        dto.setPista1("Género: " + genero);
        dto.setPista2("Director: " + director);
        dto.setPista3("Año: " + anio);
        dto.setPoster(poster);
        dto.setSinopsis(sinopsis);

        return List.of(dto);
    }
}