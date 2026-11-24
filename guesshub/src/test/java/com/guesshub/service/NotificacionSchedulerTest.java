package com.guesshub.service;

import com.guesshub.entity.Puntaje;
import com.guesshub.entity.security.Usuario;
import com.guesshub.repository.PuntajeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionSchedulerTest {

    @Mock
    private PuntajeRepository puntajeRepository;

    @Mock
    private NotificacionEmailService notificacionEmailService;

    @InjectMocks
    private NotificacionScheduler notificacionScheduler;

    @Test
    @DisplayName("Envía alerta de racha en riesgo a usuario que jugó ayer y no hoy")
    void testAlertaRachaEnRiesgo() {
        Usuario u1 = new Usuario("riesgo@test.com", "pass", "RachaRiesgo");
        Puntaje p1 = new Puntaje(u1);
        p1.setRachaDias(5);
        p1.setUltimaPartidaFecha(LocalDate.now().minusDays(1)); // jugó ayer
        p1.setRachaCongelada(false);

        // Usuario que ya jugó hoy (no debe recibir alerta)
        Usuario u2 = new Usuario("ok@test.com", "pass", "JugadorAlDia");
        Puntaje p2 = new Puntaje(u2);
        p2.setRachaDias(2);
        p2.setUltimaPartidaFecha(LocalDate.now());

        // Usuario con racha congelada (protegido, no debe recibir alerta)
        Usuario u3 = new Usuario("congelado@test.com", "pass", "RachaCongelada");
        Puntaje p3 = new Puntaje(u3);
        p3.setRachaDias(4);
        p3.setUltimaPartidaFecha(LocalDate.now().minusDays(1));
        p3.setRachaCongelada(true);

        when(puntajeRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        notificacionScheduler.verificarRachasEnRiesgo();

        verify(notificacionEmailService, times(1))
                .notificarRachaEnRiesgo("riesgo@test.com", "RachaRiesgo", 5);
        verify(notificacionEmailService, never())
                .notificarRachaEnRiesgo(eq("ok@test.com"), anyString(), anyInt());
        verify(notificacionEmailService, never())
                .notificarRachaEnRiesgo(eq("congelado@test.com"), anyString(), anyInt());
    }
}
