package com.guesshub.service;

import com.guesshub.dto.CancionDTO;
import com.guesshub.dto.DesafioDiarioDTO;
import com.guesshub.dto.EquipoDTO;
import com.guesshub.dto.PeliculaDTO;
import com.guesshub.entity.DesafioDiario;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.DesafioDiarioRepository;
import com.guesshub.repository.PartidaRepository;
import com.guesshub.repository.security.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesafioDiarioService {

    private final DesafioDiarioRepository desafioDiarioRepository;
    private final PartidaRepository partidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PreguntaActivaService preguntaActivaService;
    private final PeliculaService peliculaService;
    private final CancionService cancionService;
    private final FutbolService futbolService;

    // Cache del preguntaId efímero registrado en PreguntaActivaService para el día actual
    private volatile String preguntaIdDiarioActual = null;
    private volatile LocalDate fechaPreguntaIdActual = null;

    @Transactional
    public DesafioDiarioDTO obtenerDesafioHoy() {
        LocalDate hoy = LocalDate.now();

        DesafioDiario desafio = desafioDiarioRepository.findByFecha(hoy)
                .orElseGet(() -> generarYGuardarDesafio(hoy));

        // Asegurar que la pregunta esté activa en PreguntaActivaService para hoy
        String preguntaId = asegurarPreguntaActiva(desafio, hoy);

        boolean yaJugado = false;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.equals("anonymousUser")) {
            Optional<Usuario> userOpt = usuarioRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                yaJugado = partidaRepository.existsByUsuarioIdAndFechaAndEsDesafioDiarioTrue(userOpt.get().getId(), hoy);
            }
        }

        String nombreCat = switch (desafio.getCategoria().toLowerCase()) {
            case "peliculas", "pelicula" -> "Películas";
            case "musica", "cancion" -> "Música";
            case "deportes", "equipo" -> "Deportes";
            default -> "Desafío Diario";
        };

        String icono = switch (desafio.getCategoria().toLowerCase()) {
            case "peliculas", "pelicula" -> "film";
            case "musica", "cancion" -> "music";
            case "deportes", "equipo" -> "ball";
            default -> "dice";
        };

        return new DesafioDiarioDTO(
                preguntaId,
                desafio.getFecha(),
                desafio.getCategoria(),
                nombreCat,
                icono,
                "¿Qué " + (desafio.getCategoria().contains("peli") ? "película" : (desafio.getCategoria().contains("mus") || desafio.getCategoria().contains("cancion")) ? "canción" : "equipo") + " es?",
                List.of(desafio.getPista1(), desafio.getPista2() != null ? desafio.getPista2() : "", desafio.getPista3() != null ? desafio.getPista3() : ""),
                List.of("Pista inicial", desafio.getPista2Etiqueta() != null ? desafio.getPista2Etiqueta() : "Pista 2", desafio.getPista3Etiqueta() != null ? desafio.getPista3Etiqueta() : "Pista 3"),
                desafio.getDificultad(),
                yaJugado
        );
    }

    private synchronized String asegurarPreguntaActiva(DesafioDiario desafio, LocalDate hoy) {
        if (fechaPreguntaIdActual == null || !fechaPreguntaIdActual.equals(hoy) || preguntaIdDiarioActual == null) {
            preguntaIdDiarioActual = preguntaActivaService.registrar(desafio.getTituloCorrecto(), true);
            fechaPreguntaIdActual = hoy;
            log.info("Desafío Diario registrado en PreguntaActiva con ID {}", preguntaIdDiarioActual);
        }
        return preguntaIdDiarioActual;
    }

    private DesafioDiario generarYGuardarDesafio(LocalDate fecha) {
        int rotacion = (int) Math.abs(fecha.toEpochDay() % 3);
        try {
            if (rotacion == 0) {
                // Película
                PeliculaDTO dto = peliculaService.obtenerPeliculaRandom();
                // OMDb entrega las pistas; para obtener el título real usamos la validación
                // En PeliculaService dto.getId() ya registró la respuesta
                String titulo = obtenerTituloOIdentificador(dto.getId());
                if (titulo != null) {
                    return desafioDiarioRepository.save(new DesafioDiario(
                            fecha, "pelicula", titulo,
                            dto.getPista1(), dto.getPista2(), dto.getPista3(),
                            "Director", "Año", "MEDIO"
                    ));
                }
            } else if (rotacion == 1) {
                // Canción
                List<CancionDTO> canciones = cancionService.obtenerTrackRandom();
                if (!canciones.isEmpty()) {
                    CancionDTO dto = canciones.get(0);
                    String titulo = obtenerTituloOIdentificador(dto.getId());
                    if (titulo != null) {
                        return desafioDiarioRepository.save(new DesafioDiario(
                                fecha, "cancion", titulo,
                                dto.getPista1(), dto.getPista2(), dto.getPista3(),
                                "Álbum", "Año", "MEDIO"
                        ));
                    }
                }
            } else {
                // Deporte
                List<EquipoDTO> equipos = futbolService.obtenerEquipoRandom();
                if (!equipos.isEmpty()) {
                    EquipoDTO dto = equipos.get(0);
                    String titulo = obtenerTituloOIdentificador(dto.getId());
                    if (titulo != null) {
                        return desafioDiarioRepository.save(new DesafioDiario(
                                fecha, "equipo", titulo,
                                "País: " + dto.getPais(), "Fundación: " + dto.getFundacion(), "Estadio: " + dto.getEstadio(),
                                "Fundación", "Estadio", "MEDIO"
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error al autogenerar desafío diario desde servicios externos: {}. Usando fallback.", e.getMessage());
        }

        // Fallback robusto garantizado si los servicios externos no responden o están rate-limited
        return desafioDiarioRepository.save(new DesafioDiario(
                fecha, "pelicula", "El Padrino",
                "Un capo de la mafia de Nueva York transfiere el control de su imperio clandestino a su reacio hijo.",
                "Francis Ford Coppola",
                "1972",
                "Director",
                "Año",
                "MEDIO"
        ));
    }

    private String obtenerTituloOIdentificador(String preguntaId) {
        // Obtenemos el texto que PreguntaActivaService tiene registrado sin consumirlo
        return preguntaActivaService.peekRespuesta(preguntaId);
    }
}
