package com.guesshub.service;

import java.net.http.HttpClient;

/**
 * @deprecated Esta clase fue renombrada a {@link CancionService} para reflejar
 * su verdadero propósito (música/canciones). Utilizar {@link CancionService}.
 */
@Deprecated
public class EquipoService extends CancionService {
    public EquipoService(LastFmService lastFmService, PreguntaActivaService preguntaActivaService, HttpClient httpClient) {
        super(lastFmService, preguntaActivaService, httpClient);
    }
}
