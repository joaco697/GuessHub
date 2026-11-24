package com.guesshub.service;

import com.guesshub.dto.InsigniaDTO;
import com.guesshub.entity.Insignia;
import com.guesshub.entity.Puntaje;
import com.guesshub.entity.UsuarioInsignia;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.InsigniaRepository;
import com.guesshub.repository.UsuarioInsigniaRepository;
import com.guesshub.repository.security.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsigniaService {

    private final InsigniaRepository insigniaRepository;
    private final UsuarioInsigniaRepository usuarioInsigniaRepository;
    private final UsuarioRepository usuarioRepository;

    @PostConstruct
    public void initInsignias() {
        crearSiNoExiste("RACHA_3", "Constante", "3 días seguidos jugando", "🔥", "RACHA");
        crearSiNoExiste("RACHA_7", "Semana de Fuego", "7 días seguidos jugando", "⚡", "RACHA");
        crearSiNoExiste("RACHA_30", "Leyenda Mensual", "1 mes consecutivo jugando", "👑", "RACHA");
        crearSiNoExiste("RACHA_PARTIDA_3", "En Racha", "3 respuestas correctas seguidas", "🎯", "PARTIDA");
        crearSiNoExiste("RACHA_PARTIDA_5", "Imparable", "5 respuestas correctas seguidas", "🚀", "PARTIDA");
        crearSiNoExiste("CINEFILO", "Cinéfilo", "1000+ puntos en Películas", "🎬", "CATEGORIA");
        crearSiNoExiste("MELOMANO", "Melómano", "1000+ puntos en Música", "🎵", "CATEGORIA");
        crearSiNoExiste("DEPORTISTA", "Crack", "1000+ puntos en Deportes", "⚽", "CATEGORIA");
        crearSiNoExiste("TOP_10", "Entre los Mejores", "Alcanzaste el Top 10 del ranking", "🏆", "RANKING");
    }

    private void crearSiNoExiste(String codigo, String nombre, String desc, String icono, String cat) {
        if (insigniaRepository.findByCodigo(codigo).isEmpty()) {
            insigniaRepository.save(new Insignia(codigo, nombre, desc, icono, cat));
        }
    }

    @Transactional
    public List<String> evaluarInsignias(Usuario usuario, Puntaje puntaje, int rachaPartida, int posicionRanking) {
        List<String> nuevas = new ArrayList<>();
        Set<String> yaObtenidas = usuarioInsigniaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(ui -> ui.getInsignia().getCodigo())
                .collect(Collectors.toSet());

        // Racha de días
        if (puntaje.getRachaDias() >= 3) checkYOtorgar(usuario, "RACHA_3", yaObtenidas, nuevas);
        if (puntaje.getRachaDias() >= 7) checkYOtorgar(usuario, "RACHA_7", yaObtenidas, nuevas);
        if (puntaje.getRachaDias() >= 30) checkYOtorgar(usuario, "RACHA_30", yaObtenidas, nuevas);

        // Racha en la partida
        if (rachaPartida >= 3) checkYOtorgar(usuario, "RACHA_PARTIDA_3", yaObtenidas, nuevas);
        if (rachaPartida >= 5) checkYOtorgar(usuario, "RACHA_PARTIDA_5", yaObtenidas, nuevas);

        // Categorías
        if (puntaje.getPuntosPeliculas() >= 1000) checkYOtorgar(usuario, "CINEFILO", yaObtenidas, nuevas);
        if (puntaje.getPuntosMusica() >= 1000) checkYOtorgar(usuario, "MELOMANO", yaObtenidas, nuevas);
        if (puntaje.getPuntosDeportes() >= 1000) checkYOtorgar(usuario, "DEPORTISTA", yaObtenidas, nuevas);

        // Ranking
        if (posicionRanking > 0 && posicionRanking <= 10) {
            checkYOtorgar(usuario, "TOP_10", yaObtenidas, nuevas);
        }

        return nuevas;
    }

    private void checkYOtorgar(Usuario usuario, String codigo, Set<String> yaObtenidas, List<String> nuevas) {
        if (!yaObtenidas.contains(codigo)) {
            insigniaRepository.findByCodigo(codigo).ifPresent(insignia -> {
                usuarioInsigniaRepository.save(new UsuarioInsignia(usuario, insignia));
                nuevas.add(insignia.getNombre());
                // Si el usuario no tiene ninguna insignia equipada, equiparle la primera automáticamente
                if (usuario.getInsigniaEquipada() == null) {
                    usuario.setInsigniaEquipada(insignia);
                    usuarioRepository.save(usuario);
                }
                log.info("Insignia '{}' otorgada al usuario {}", insignia.getNombre(), usuario.getUsername());
            });
        }
    }

    @Transactional(readOnly = true)
    public List<InsigniaDTO> listarParaUsuario(Usuario usuario) {
        List<Insignia> todas = insigniaRepository.findAll();
        if (usuario == null) {
            return todas.stream()
                    .map(i -> new InsigniaDTO(i.getId(), i.getCodigo(), i.getNombre(),
                            i.getDescripcion(), i.getIcono(), i.getCategoria(), false, false))
                    .toList();
        }

        Set<Long> idsDesbloqueadas = usuarioInsigniaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(ui -> ui.getInsignia().getId())
                .collect(Collectors.toSet());

        Long equipadaId = usuario.getInsigniaEquipada() != null ? usuario.getInsigniaEquipada().getId() : null;

        return todas.stream()
                .map(i -> new InsigniaDTO(
                        i.getId(),
                        i.getCodigo(),
                        i.getNombre(),
                        i.getDescripcion(),
                        i.getIcono(),
                        i.getCategoria(),
                        idsDesbloqueadas.contains(i.getId()),
                        equipadaId != null && equipadaId.equals(i.getId())
                ))
                .toList();
    }

    @Transactional
    public void equiparInsignia(Usuario usuario, Long insigniaId) {
        if (insigniaId == null) {
            usuario.setInsigniaEquipada(null);
            usuarioRepository.save(usuario);
            return;
        }

        Insignia insignia = insigniaRepository.findById(insigniaId)
                .orElseThrow(() -> new IllegalArgumentException("Insignia no encontrada"));

        boolean poseeInsignia = usuarioInsigniaRepository.findByUsuarioId(usuario.getId())
                .stream()
                .anyMatch(ui -> ui.getInsignia().getId().equals(insigniaId));

        if (!poseeInsignia) {
            throw new IllegalStateException("No posees esta insignia para equiparla");
        }

        usuario.setInsigniaEquipada(insignia);
        usuarioRepository.save(usuario);
    }
}

