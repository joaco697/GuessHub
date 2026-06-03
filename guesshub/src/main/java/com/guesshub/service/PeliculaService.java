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
    private static final int MAX_INTENTOS = 500;

    public List<PeliculaDTO> obtenerPeliculaRandom() throws Exception {

        JSONObject json = null;
        int intentos = 0;

        while (intentos < MAX_INTENTOS) {

            intentos++;

            String imdbId = String.format(
                    "tt%07d",
                    ThreadLocalRandom.current()
                            .nextInt(2000000, 9000000)
            );

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

            // No existe
            if (!json.optString("Response").equals("True")) {
                continue;
            }

            // No es una película
            if (!"movie".equalsIgnoreCase(json.optString("Type"))) {
                continue;
            }

            String votosStr = json.optString("imdbVotes", "0");

            int votos = Integer.parseInt(
                    votosStr.replace(",", "")
            );

            // Tiene suficientes votos
            if (votos >= MIN_VOTOS) {
                break;
            }
        }

        if (json == null ||
            !json.optString("Response").equals("True")) {

            throw new RuntimeException(
                    "No se encontró una película válida después de "
                    + MAX_INTENTOS + " intentos."
            );
        }

        PeliculaDTO dto = new PeliculaDTO();

        dto.setId(0);

        dto.setNombre(
                json.optString("Title", "Desconocido")
        );

        dto.setGenero(
                json.optString("Genre", "Desconocido")
        );

        try {
            dto.setAño(
                    Integer.parseInt(
                            json.optString("Year", "0")
                                    .replaceAll("[^0-9]", "")
                    )
            );
        } catch (Exception e) {
            dto.setAño(0);
        }

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