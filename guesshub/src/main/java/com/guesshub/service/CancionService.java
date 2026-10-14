package com.guesshub.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.guesshub.dto.CancionDTO;
import com.guesshub.service.LastFmService.CancionLastFm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para la categoría Música.
 * Maneja la obtención de pistas y datos de canciones en vivo
 * combinando Last.fm (títulos populares) y Deezer (metadatos y audio previews).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancionService {

    private final LastFmService lastFmService;
    private final PreguntaActivaService preguntaActivaService;
    private final HttpClient httpClient;

    @Value("${deezer.api.url}")
    private String deezerUrl;

    /**
     * 1. Obtiene una canción aleatoria en vivo desde Last.fm (sin tocar la base)
     * 2. La busca en Deezer para obtener portada, álbum, fecha y preview de audio
     * 3. Registra la respuesta correcta del lado del servidor y devuelve el
     *    DTO con pistas — el nombre de la canción nunca viaja al cliente.
     */
    public List<CancionDTO> obtenerTrackRandom() throws Exception {

        CancionLastFm cancion = lastFmService.obtenerAleatoriaLive();

        String query = URLEncoder.encode(cancion.nombre() + " " + cancion.artista(),
                StandardCharsets.UTF_8);
        String url = deezerUrl + "/search?q=" + query + "&limit=1";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        JSONArray data = json.optJSONArray("data");

        CancionDTO dto = new CancionDTO();
        String preguntaId = preguntaActivaService.registrar(cancion.nombre());
        dto.setId(preguntaId);

        if (data == null || data.isEmpty()) {
            log.warn("Deezer no encontró resultados para: {} - {}", cancion.nombre(), cancion.artista());
            dto.setPista1("El artista es: " + cancion.artista());
            dto.setPista2("No se encontró info del álbum");
            dto.setPista3("No se encontró la fecha");
            dto.setLogo("");
            dto.setPreview("");
            return List.of(dto);
        }

        JSONObject track = data.getJSONObject(0);

        JSONObject artist = track.getJSONObject("artist");
        dto.setPista1("El artista es: " + artist.getString("name"));

        JSONObject album = track.getJSONObject("album");
        dto.setPista2("Pertenece al álbum: " + album.getString("title"));

        String trackId = String.valueOf(track.getInt("id"));
        dto.setPista3(obtenerFechaLanzamiento(trackId));

        dto.setLogo(album.optString("cover_big", ""));
        dto.setPreview(track.optString("preview", ""));

        return List.of(dto);
    }

    /**
     * Consulta el endpoint de track individual de Deezer para obtener la fecha de lanzamiento.
     */
    private String obtenerFechaLanzamiento(String trackId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(deezerUrl + "/track/" + trackId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            String fecha = json.optString("release_date", "");
            return fecha.isEmpty() ? "Fecha desconocida" : "Año de lanzamiento: " + fecha.substring(0, 4);
        } catch (Exception e) {
            log.warn("No se pudo obtener fecha de lanzamiento para track {}", trackId);
            return "Fecha desconocida";
        }
    }
}
