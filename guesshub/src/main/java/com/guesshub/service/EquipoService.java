package com.guesshub.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.guesshub.dto.EquipoDTO;

@Service
public class EquipoService {

    private static final String BASE_URL = "https://api.deezer.com/track/";

    public List<EquipoDTO> obtenerTrackRandom() throws Exception {

        int minimo = 508000;
        int maximo = 100000000;

        List<EquipoDTO> tracks = new ArrayList<>();

        JSONObject json = null;
        int rank = Integer.MAX_VALUE;

        while (rank > 1000) {

            int idRandom = ThreadLocalRandom.current().nextInt(minimo, maximo + 1);

            String url = BASE_URL + idRandom;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            json = new JSONObject(response.body());

            // Si no existe el rank seguimos buscando
            if (!json.has("rank")) {
                continue;
            }

            rank = json.getInt("rank");

            // Si cumple la condición cortamos el loop
            if (rank <= 1000) {
                break;
            }
        }

        EquipoDTO dto = new EquipoDTO();

        dto.setId(json.getInt("id"));

        dto.setNombre("Respuesta correcta: " + json.getString("title"));

        JSONObject artistObj = json.getJSONObject("artist");
        dto.setPista1("La canción es de " + artistObj.getString("name"));

        JSONObject albumObj = json.getJSONObject("album");
        dto.setPista2("Es del álbum  " + albumObj.getString("title"));

        dto.setPista3("El año de lanzamiento fue en " + json.getString("release_date"));

        dto.setLogo(albumObj.getString("cover_big"));

        tracks.add(dto);

        return tracks;
    }
}