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
 * identificada por un preguntaId random (UUID) que es lo único que viaja al cliente.
 *
 * Reemplaza la validación client-side que tenían Música y Deportes (y el leak de
 * "titulo" que Película mandaba en el JSON de /random aunque no lo usara el frontend):
 * ninguna categoría expone la respuesta correcta en ningún momento antes de validar.
 *
 * Es intencionalmente simple (un Map en memoria, sin persistencia) porque las
 * preguntas son efímeras: viven desde que se piden hasta que se contestan o expiran.
 * Si el backend corriera en más de una instancia, esto tendría que migrar a algo
 * compartido (Redis) — hoy no es el caso (ver Parte 1, sección 7).
 */
@Slf4j
@Service
public class PreguntaActivaService {

    private static final Duration EXPIRACION = Duration.ofMinutes(10);

    private final Map<String, PreguntaActiva> preguntas = new ConcurrentHashMap<>();

    private record PreguntaActiva(String respuestaCorrecta, Instant creada) {}

    /**
     * Registra la respuesta correcta de una pregunta nueva y devuelve el id
     * (UUID) que el cliente va a usar para, más adelante, contestarla.
     */
    public String registrar(String respuestaCorrecta) {
        limpiarExpiradas();
        String id = UUID.randomUUID().toString();
        preguntas.put(id, new PreguntaActiva(respuestaCorrecta, Instant.now()));
        return id;
    }

    /**
     * Valida la respuesta del usuario contra la guardada en el servidor.
     * La pregunta se consume al validarla (no se puede reintentar la misma).
     */
    public ResultadoDTO validar(String preguntaId, String respuestaUsuario) {
        PreguntaActiva pa = preguntas.remove(preguntaId);

        if (pa == null) {
            return new ResultadoDTO(false, "—",
                    "Esta pregunta ya expiró o ya fue respondida. Pedí una nueva.");
        }

        boolean correcto = normalizar(respuestaUsuario).equals(normalizar(pa.respuestaCorrecta()));

        return new ResultadoDTO(
                correcto,
                pa.respuestaCorrecta(),
                correcto ? "¡Correcto!" : "Incorrecto, la respuesta era: " + pa.respuestaCorrecta()
        );
    }

    private void limpiarExpiradas() {
        Instant limite = Instant.now().minus(EXPIRACION);
        int antes = preguntas.size();
        preguntas.entrySet().removeIf(e -> e.getValue().creada().isBefore(limite));
        int removidas = antes - preguntas.size();
        if (removidas > 0) {
            log.debug("Limpieza de preguntas activas: {} expiradas removidas", removidas);
        }
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinTildes.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}
