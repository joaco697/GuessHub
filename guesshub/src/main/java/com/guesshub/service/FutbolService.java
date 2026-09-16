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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.guesshub.dto.EquipoDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FutbolService {

    private final PreguntaActivaService preguntaActivaService;

    @Value("${apisports.api.key}")
    private String apiKey;

    @Value("${apisports.api.url}")
    private String apiUrl;

    private static final int[] LIGAS = {
            39,
            78,
            140,
            135,
            61
    };

    /**
     * Trae un equipo al azar en vivo desde api-sports.io y registra su nombre
     * como respuesta correcta del lado del servidor — nunca viaja al cliente
     * (ver PreguntaActivaService).
     */
    public List<EquipoDTO> obtenerEquipoRandom() throws Exception {

        int ligaRandom = LIGAS[ThreadLocalRandom.current().nextInt(LIGAS.length)];

        String url = apiUrl + "/teams?league=" + ligaRandom + "&season=2024";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-apisports-key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        JSONArray equipos = json.getJSONArray("response");

        int indiceRandom = ThreadLocalRandom.current().nextInt(equipos.length());
        JSONObject equipoResponse = equipos.getJSONObject(indiceRandom);
        JSONObject equipo = equipoResponse.getJSONObject("team");
        JSONObject venue = equipoResponse.getJSONObject("venue");

        String nombre = equipo.getString("name");

        EquipoDTO dto = new EquipoDTO();
        dto.setId(preguntaActivaService.registrar(nombre));
        dto.setPais(equipo.getString("country"));
        dto.setFundacion(equipo.getInt("founded"));
        dto.setLogo(equipo.getString("logo"));
        dto.setEstadio(venue.getString("name"));
        dto.setCiudad(venue.optString("city", ""));

        List<EquipoDTO> equiposDTO = new ArrayList<>();
        equiposDTO.add(dto);
        return equiposDTO;
    }
}
