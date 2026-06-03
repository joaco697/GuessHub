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

import com.guesshub.dto.PeliculaDTO;

@Service
public class PeliculaService {

    private static final String API_KEY = "d8c63221";
    private static final String BASE_URL =
            "https://www.omdbapi.com/?apikey=" + API_KEY + "&i=";

    private static final int MIN_VOTOS = 100000;

    public List<PeliculaDTO> obtenerPeliculaRandom() throws Exception {

        JSONObject json = null;

        while (true) {

            String imdbId = String.format(
                    "tt%07d",
                    ThreadLocalRandom.current()
                            .nextInt(1, 9999999));

            String url = BASE_URL + imdbId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient()
                            .send(request,
                                    HttpResponse.BodyHandlers.ofString());

            json = new JSONObject(response.body());

           
            if (!json.optString("Response").equals("True")) {
                continue;
            }

          
            if (!"movie".equalsIgnoreCase(json.optString("Type"))) {
                continue;
            }

           
            String votosStr = json.optString("imdbVotes", "0");

            int votos = Integer.parseInt(
                    votosStr.replace(",", "")
            );

        
            if (votos >= MIN_VOTOS) {
                break;
            }
        }

        PeliculaDTO dto = new PeliculaDTO();

        dto.setId(0);
        dto.setNombre(json.getString("Title"));

        dto.setGenero(
                json.optString("Genre", "Sin género")
        );

        dto.setAño(
                Integer.parseInt(
                        json.getString("Year")
                                .replaceAll("[^0-9]", "")
                )
        );

        dto.setPoster(
                json.optString("Poster", "")
        );

        dto.setDirector(
                json.optString("Director", "Desconocido")
        );

        List<PeliculaDTO> peliculas = new ArrayList<>();
        peliculas.add(dto);

        return peliculas;
    }
}