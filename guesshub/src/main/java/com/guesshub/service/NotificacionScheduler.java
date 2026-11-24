package com.guesshub.service;

import com.guesshub.entity.Puntaje;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.PuntajeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionScheduler {

    private final PuntajeRepository puntajeRepository;
    private final NotificacionEmailService notificacionEmailService;

    /**
     * Revisa diariamente a las 20:00 (o cada período programado) qué usuarios tienen racha en riesgo
     * (jugaron ayer, tienen racha > 0 y todavía no jugaron hoy ni tienen la racha congelada).
     */
    @Scheduled(cron = "${guesshub.notifications.racha-cron:0 0 20 * * *}")
    public void verificarRachasEnRiesgo() {
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        List<Puntaje> todos = puntajeRepository.findAll();
        int avisos = 0;

        for (Puntaje p : todos) {
            Usuario u = p.getUsuario();
            if (u == null || u.getUsername() == null) continue;

            // Si jugó ayer, tiene racha > 0, no jugó hoy y no congeló su racha
            if (ayer.equals(p.getUltimaPartidaFecha()) && p.getRachaDias() > 0 && !p.isRachaCongelada()) {
                notificacionEmailService.notificarRachaEnRiesgo(u.getUsername(), u.getDisplayName(), p.getRachaDias());
                avisos++;
            }
        }

        if (avisos > 0) {
            log.info("Notificaciones de racha en riesgo enviadas a {} usuarios", avisos);
        }
    }

    /**
     * Revisa semanalmente usuarios inactivos (sin jugar en los últimos 7 días).
     */
    @Scheduled(cron = "${guesshub.notifications.inactivos-cron:0 0 12 ? * MON}")
    public void verificarInactividad() {
        LocalDate limite = LocalDate.now().minusDays(7);
        List<Puntaje> todos = puntajeRepository.findAll();

        for (Puntaje p : todos) {
            Usuario u = p.getUsuario();
            if (u == null || u.getUsername() == null) continue;

            if (p.getUltimaPartidaFecha() != null && p.getUltimaPartidaFecha().isBefore(limite)) {
                notificacionEmailService.notificarInactividad(u.getUsername(), u.getDisplayName());
            }
        }
    }
}
