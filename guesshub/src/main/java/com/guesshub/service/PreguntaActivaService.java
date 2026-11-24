package com.guesshub.service;

import com.guesshub.dto.ResultadoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guarda la respuesta correcta de una pregunta en curso del lado del servidor,
 * identificada por un preguntaId random (UUID).
 */
@Slf4j
@Service
public class PreguntaActivaService {

    private static final Duration EXPIRACION = Duration.ofMinutes(10);
    private static final Duration EXPIRACION_DIARIA = Duration.ofHours(36);

    private final Map<String, PreguntaActiva> preguntas = new ConcurrentHashMap<>();

    private record PreguntaActiva(String respuestaCorrecta, Instant creada, boolean compartida) {}

    /**
     * Registra la respuesta de una pregunta efímera de partida normal.
     */
    public String registrar(String respuestaCorrecta) {
        return registrar(respuestaCorrecta, false);
    }

    /**
     * Registra una pregunta, opcionalmente compartida (ej. desafío diario).
     */
    public String registrar(String respuestaCorrecta, boolean compartida) {
        limpiarExpiradas();
        String id = UUID.randomUUID().toString();
        preguntas.put(id, new PreguntaActiva(respuestaCorrecta, Instant.now(), compartida));
        return id;
    }

    /**
     * Consulta la respuesta sin consumirla (utilizado por generadores de desafíos).
     */
    public String peekRespuesta(String preguntaId) {
        PreguntaActiva pa = preguntas.get(preguntaId);
        return pa != null ? pa.respuestaCorrecta() : null;
    }

    /**
     * Valida la respuesta del usuario contra la guardada en el servidor.
     * Si no es compartida, se consume al responder.
     */
    public ResultadoDTO validar(String preguntaId, String respuestaUsuario) {
        PreguntaActiva pa = preguntas.get(preguntaId);

        if (pa == null) {
            return new ResultadoDTO(false, "—",
                    "Esta pregunta ya expiró o ya fue respondida. Pedí una nueva.");
        }

        if (!pa.compartida()) {
            preguntas.remove(preguntaId);
        }

        boolean correcto = normalizar(respuestaUsuario).equals(normalizar(pa.respuestaCorrecta()));

        return new ResultadoDTO(
                correcto,
                pa.respuestaCorrecta(),
                correcto ? "¡Correcto!" : "Incorrecto, la respuesta era: " + pa.respuestaCorrecta()
        );
    }

    private void limpiarExpiradas() {
        Instant limiteNormal = Instant.now().minus(EXPIRACION);
        Instant limiteDiario = Instant.now().minus(EXPIRACION_DIARIA);

        preguntas.entrySet().removeIf(e -> {
            PreguntaActiva p = e.getValue();
            return p.compartida() ? p.creada().isBefore(limiteDiario) : p.creada().isBefore(limiteNormal);
        });
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinTildes.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}
