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
import com.guesshub.entity.Equipo;
import com.guesshub.repository.EquipoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FutbolService {

    private final PreguntaActivaService preguntaActivaService;
    private final EquipoRepository equipoRepository;
    private final HttpClient httpClient;

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

    private record FallbackEquipo(String nombre, String pais, int fundacion, String logo, String estadio, String ciudad) {}

    private static final List<FallbackEquipo> FALLBACK_EQUIPOS = List.of(
            new FallbackEquipo("Real Madrid", "Spain", 1902, "https://media.api-sports.io/football/teams/541.png", "Santiago Bernabéu", "Madrid"),
            new FallbackEquipo("Barcelona", "Spain", 1899, "https://media.api-sports.io/football/teams/529.png", "Spotify Camp Nou", "Barcelona"),
            new FallbackEquipo("Boca Juniors", "Argentina", 1905, "https://media.api-sports.io/football/teams/451.png", "La Bombonera", "Buenos Aires"),
            new FallbackEquipo("River Plate", "Argentina", 1901, "https://media.api-sports.io/football/teams/435.png", "El Monumental", "Buenos Aires"),
            new FallbackEquipo("Manchester City", "England", 1880, "https://media.api-sports.io/football/teams/50.png", "Etihad Stadium", "Manchester"),
            new FallbackEquipo("Liverpool", "England", 1892, "https://media.api-sports.io/football/teams/40.png", "Anfield", "Liverpool"),
            new FallbackEquipo("Bayern Munich", "Germany", 1900, "https://media.api-sports.io/football/teams/157.png", "Allianz Arena", "Munich"),
            new FallbackEquipo("Juventus", "Italy", 1897, "https://media.api-sports.io/football/teams/496.png", "Allianz Stadium", "Torino"),
            new FallbackEquipo("Paris Saint Germain", "France", 1970, "https://media.api-sports.io/football/teams/85.png", "Parc des Princes", "Paris"),
            new FallbackEquipo("Arsenal", "England", 1886, "https://media.api-sports.io/football/teams/42.png", "Emirates Stadium", "London")
    );

    /**
     * Trae un equipo al azar en vivo desde api-sports.io y registra su nombre
     * como respuesta correcta del lado del servidor — nunca viaja al cliente
     * (ver PreguntaActivaService). Si la API falla o devuelve lista vacía,
     * utiliza un mecanismo de fallback seguro.
     */
    public List<EquipoDTO> obtenerEquipoRandom() {
        try {
            int ligaRandom = LIGAS[ThreadLocalRandom.current().nextInt(LIGAS.length)];
            String url = apiUrl + "/teams?league=" + ligaRandom + "&season=2024";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("x-apisports-key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("API Sports devolvió status {}. Usando fallback...", response.statusCode());
                return obtenerEquipoFallback();
            }

            JSONObject json = new JSONObject(response.body());
            JSONArray equipos = json.optJSONArray("response");

            if (equipos == null || equipos.isEmpty()) {
                log.warn("API Sports devolvió lista de equipos vacía. Usando fallback...");
                return obtenerEquipoFallback();
            }

            int indiceRandom = ThreadLocalRandom.current().nextInt(equipos.length());
            JSONObject equipoResponse = equipos.getJSONObject(indiceRandom);
            JSONObject equipo = equipoResponse.optJSONObject("team");
            JSONObject venue = equipoResponse.optJSONObject("venue");

            if (equipo == null || !equipo.has("name") || equipo.getString("name").isBlank()) {
                log.warn("Equipo obtenido con datos incompletos. Usando fallback...");
                return obtenerEquipoFallback();
            }

            String nombre = equipo.getString("name");

            EquipoDTO dto = new EquipoDTO();
            dto.setId(preguntaActivaService.registrar(nombre));
            dto.setPais(equipo.optString("country", "Desconocido"));
            dto.setFundacion(equipo.optInt("founded", 1900));
            dto.setLogo(equipo.optString("logo", ""));
            dto.setEstadio(venue != null ? venue.optString("name", "Estadio desconocido") : "Estadio desconocido");
            dto.setCiudad(venue != null ? venue.optString("city", "") : "");

            List<EquipoDTO> equiposDTO = new ArrayList<>();
            equiposDTO.add(dto);
            return equiposDTO;

        } catch (Exception e) {
            log.warn("Excepción al consultar API Sports: {}. Activando fallback...", e.getMessage());
            return obtenerEquipoFallback();
        }
    }

    /**
     * Fallback cuando la API de fútbol no responde o no devuelve datos.
     * Intenta consultar la BD local de equipos y, si no hay, usa una lista curada predefinida.
     */
    public List<EquipoDTO> obtenerEquipoFallback() {
        try {
            List<Equipo> locales = equipoRepository.findAll();
            if (!locales.isEmpty()) {
                Equipo eq = locales.get(ThreadLocalRandom.current().nextInt(locales.size()));
                EquipoDTO dto = new EquipoDTO();
                dto.setId(preguntaActivaService.registrar(eq.getNombre()));
                dto.setPais(eq.getPais() != null ? eq.getPais() : "Desconocido");
                dto.setFundacion(eq.getFundacion() != null ? eq.getFundacion() : 1900);
                dto.setLogo(eq.getLogo() != null ? eq.getLogo() : "");
                dto.setEstadio(eq.getEstadio() != null ? eq.getEstadio() : "Estadio desconocido");
                dto.setCiudad(eq.getCiudad() != null ? eq.getCiudad() : "");
                return List.of(dto);
            }
        } catch (Exception ex) {
            log.warn("No se pudo obtener equipo desde la base de datos local: {}", ex.getMessage());
        }

        // Fallback predefinido en memoria
        FallbackEquipo fb = FALLBACK_EQUIPOS.get(ThreadLocalRandom.current().nextInt(FALLBACK_EQUIPOS.size()));
        EquipoDTO dto = new EquipoDTO();
        dto.setId(preguntaActivaService.registrar(fb.nombre()));
        dto.setPais(fb.pais());
        dto.setFundacion(fb.fundacion());
        dto.setLogo(fb.logo());
        dto.setEstadio(fb.estadio());
        dto.setCiudad(fb.ciudad());
        return List.of(dto);
    }
}

