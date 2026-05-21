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

    public List<EquipoDTO> obtenerEquiposPorNombre(String nombreEquipo) throws Exception {

        int minimo = 508000;
        int maximo = 100000000;

        int idRandom = ThreadLocalRandom.current().nextInt(minimo, maximo + 1);

        String url = BASE_URL + idRandom;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());

        List<EquipoDTO> equipos = new ArrayList<>();

        EquipoDTO dto = new EquipoDTO();

        // ID de la canción
        dto.setId(json.getInt("id"));

        // Título de la canción
        dto.setNombre("Título: " + json.getString("title"));

        // Nombre del cantante/artista
        JSONObject artistObj = json.getJSONObject("artist");
        dto.setPais("Artista: " + artistObj.getString("name"));

        // Nombre del álbum
        JSONObject albumObj = json.getJSONObject("album");
        dto.setEstadio("Álbum: " + albumObj.getString("title"));

        // Fecha de lanzamiento
        dto.setDireccionEstadio("Lanzamiento: " + json.getString("release_date"));

        // Cover del álbum
        dto.setLogo(albumObj.getString("cover_big"));

        equipos.add(dto);

        return equipos;
    }
}