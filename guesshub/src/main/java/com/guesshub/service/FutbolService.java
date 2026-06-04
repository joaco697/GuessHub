package com.guesshub.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.guesshub.dto.EquipoDTO;

@Service
public class FutbolService {

    private static final String API_KEY = "c9ae24e836d3d8248578568d6d68cce7";

    private static final int[] LIGAS = {
            39,   
            78,   
            140,  
            135,  
            61    
    };

    public List<EquipoDTO> obtenerEquipoRandom() throws Exception {

        int ligaRandom = LIGAS[
                ThreadLocalRandom.current()
                        .nextInt(LIGAS.length)
        ];

        String url =
                "https://v3.football.api-sports.io/teams?league="
                        + ligaRandom
                        + "&season=2024";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-apisports-key", API_KEY)
                .GET()
                .build();

        HttpResponse<String> response =
                HttpClient.newHttpClient()
                        .send(request,
                                HttpResponse.BodyHandlers.ofString());

        JSONObject json =
                new JSONObject(response.body());

        JSONArray equipos =
                json.getJSONArray("response");

        int indiceRandom =
                ThreadLocalRandom.current()
                        .nextInt(equipos.length());

        JSONObject equipoResponse =
                equipos.getJSONObject(indiceRandom);

        JSONObject equipo =
                equipoResponse.getJSONObject("team");

        JSONObject venue =
                equipoResponse.getJSONObject("venue");

        EquipoDTO dto = new EquipoDTO();

        dto.setId(
                equipo.getInt("id")
        );

        dto.setNombre(
                equipo.getString("name")
        );

        dto.setPais(
                equipo.getString("country")
        );

        dto.setFundacion(
                equipo.getInt("founded")
        );

        dto.setLogo(
                equipo.getString("logo")
        );

        dto.setEstadio(
                venue.getString("name")
        );

        dto.setCiudad(
                venue.optString("city", "")
        );

        List<EquipoDTO> equiposDTO =
                new ArrayList<>();

        equiposDTO.add(dto);

        return equiposDTO;
    }
}