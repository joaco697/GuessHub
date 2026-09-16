package com.guesshub.service;

import com.guesshub.entity.Cancion;
import com.guesshub.repository.CancionRepository;
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
public class LastFmService {

    private final CancionRepository cancionRepository;

    @Value("${lastfm.api.key}")
    private String apiKey;

    @Value("${lastfm.api.url}")
    private String apiUrl;

    /** Nombre + artista de una canción, tal como los devuelve Last.fm. */
    public record CancionLastFm(String nombre, String artista) {}

    /**
     * Trae una página al azar del top tracks de Last.fm y devuelve una
     * canción al azar de esa página — en vivo, sin tocar la base de datos.
     * Mismo mecanismo que TmdbService.obtenerAleatoriaLive() y que ya usaba
     * FutbolService con api-sports.io.
     */
    public CancionLastFm obtenerAleatoriaLive() throws Exception {
        int pagina = ThreadLocalRandom.current().nextInt(1, 6);
        String url = apiUrl + "?method=chart.gettoptracks&api_key=" + apiKey
                + "&limit=50&page=" + pagina + "&format=json";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        if (!json.has("tracks")) {
            throw new RuntimeException("Respuesta inesperada de Last.fm: " + response.body());
        }
        JSONArray trackList = json.getJSONObject("tracks").getJSONArray("track");
        if (trackList.isEmpty()) {
            throw new RuntimeException("Last.fm no devolvió canciones para la página " + pagina);
        }

        JSONObject track = trackList.getJSONObject(ThreadLocalRandom.current().nextInt(trackList.length()));
        String nombre = track.getString("name");
        String artista = track.getJSONObject("artist").getString("name");
        return new CancionLastFm(nombre, artista);
    }

    /**
     * Consulta Last.fm para obtener el top 100 de canciones
     * y las persiste en Supabase (ignora duplicados).
     * @return cantidad de canciones nuevas guardadas
     *
     * Ya no la usa el flujo de juego (ver obtenerAleatoriaLive) — queda
     * disponible por si en el futuro se necesita un catálogo propio offline.
     */
    public int syncTop100() throws Exception {
        String url = apiUrl + "?method=chart.gettoptracks&api_key=" + apiKey
                + "&limit=100&format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());

        if (!json.has("tracks")) {
            throw new RuntimeException("Respuesta inesperada de Last.fm: " + response.body());
        }

        JSONArray trackList = json.getJSONObject("tracks").getJSONArray("track");

        int nuevas = 0;
        List<Cancion> toSave = new ArrayList<>();

        for (int i = 0; i < trackList.length(); i++) {
            JSONObject track = trackList.getJSONObject(i);
            String nombre  = track.getString("name");
            String artista = track.getJSONObject("artist").getString("name");

            if (!cancionRepository.existsByNombreAndArtista(nombre, artista)) {
                toSave.add(new Cancion(nombre, artista));
                nuevas++;
            }
        }

        if (!toSave.isEmpty()) {
            cancionRepository.saveAll(toSave);
            log.info("LastFm sync: {} canciones nuevas guardadas", nuevas);
        } else {
            log.info("LastFm sync: no hay canciones nuevas");
        }

        return nuevas;
    }
}