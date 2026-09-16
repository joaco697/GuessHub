package com.guesshub.service;

import com.guesshub.entity.Pelicula;
import com.guesshub.repository.PeliculaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    private final PeliculaRepository peliculaRepository;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.url}")
    private String apiUrl;

    /** Título + título original de una película, tal como los devuelve TMDB. */
    public record PeliculaTmdb(String titulo, String tituloOriginal) {}

    /**
     * Trae una página al azar (1 a 10) del top_rated de TMDB y devuelve una
     * película al azar de esa página — en vivo, sin tocar la base de datos.
     * Es exactamente el mismo mecanismo que ya usaba FutbolService con
     * api-sports.io: la propia API externa hace de "pool" de candidatos.
     */
    public PeliculaTmdb obtenerAleatoriaLive() throws Exception {
        int pagina = ThreadLocalRandom.current().nextInt(1, 11);
        String url = apiUrl + "/movie/top_rated?api_key=" + apiKey
                + "&language=es-AR&page=" + pagina;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        JSONArray results = json.optJSONArray("results");
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("TMDB no devolvió resultados para la página " + pagina);
        }

        JSONObject movie = results.getJSONObject(ThreadLocalRandom.current().nextInt(results.length()));
        String titulo = movie.optString("title", "");
        String tituloOriginal = movie.optString("original_title", titulo);
        return new PeliculaTmdb(titulo, tituloOriginal);
    }

    /**
     * Consulta TMDB top_rated (hasta 5 páginas = ~100 películas)
     * y las persiste en Supabase ignorando duplicados.
     * @return cantidad de películas nuevas guardadas
     *
     * Ya no la usa el flujo de juego (ver obtenerAleatoriaLive) — queda
     * disponible por si en el futuro se necesita un catálogo propio offline.
     */
    public int syncTop100() throws Exception {
        List<Pelicula> toSave = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();

        for (int page = 1; page <= 5; page++) {
            String url = apiUrl + "/movie/top_rated?api_key=" + apiKey
                    + "&language=es-AR&page=" + page;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());

            if (!json.has("results")) {
                log.warn("TMDB página {} sin resultados", page);
                break;
            }

            JSONArray results = json.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);
                String titulo         = movie.optString("title", "");
                String tituloOriginal = movie.optString("original_title", titulo);

                if (titulo.isBlank()) continue;

                if (!peliculaRepository.existsByTituloOriginal(tituloOriginal)) {
                    toSave.add(new Pelicula(titulo, tituloOriginal));
                }
            }

            // Si ya tenemos 100+, cortamos
            if (toSave.size() >= 100) break;
        }

        if (!toSave.isEmpty()) {
            peliculaRepository.saveAll(toSave);
            log.info("TMDB sync: {} películas nuevas guardadas", toSave.size());
        } else {
            log.info("TMDB sync: no hay películas nuevas");
        }

        return toSave.size();
    }
}