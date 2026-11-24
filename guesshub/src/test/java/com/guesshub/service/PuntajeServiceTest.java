package com.guesshub.service;

import com.guesshub.repository.PartidaRepository;
import com.guesshub.repository.PuntajeRepository;
import com.guesshub.repository.security.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PuntajeServiceTest {

    @Mock
    private PuntajeRepository puntajeRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private InsigniaService insigniaService;

    private PuntajeService puntajeService;

    @BeforeEach
    void setUp() {
        puntajeService = new PuntajeService(puntajeRepository, usuarioRepository, partidaRepository, insigniaService);
    }

    @Test
    @DisplayName("Cálculo de multiplicador base por dificultad")
    void testMultiplicadorDificultad() {
        assertEquals(1.0, puntajeService.calcularMultiplicador("FACIL", 0, 0));
        assertEquals(1.2, puntajeService.calcularMultiplicador("MEDIO", 0, 0));
        assertEquals(1.4, puntajeService.calcularMultiplicador("DIFICIL", 0, 0));
    }

    @Test
    @DisplayName("Multiplicador aumenta +0.05 por cada día de racha global")
    void testMultiplicadorRachaDias() {
        // Fácil (1.0) + 4 días * 0.05 = 1.20
        assertEquals(1.2, puntajeService.calcularMultiplicador("FACIL", 4, 0));
        // Medio (1.2) + 2 días * 0.05 = 1.30
        assertEquals(1.3, puntajeService.calcularMultiplicador("MEDIO", 2, 0));
    }

    @Test
    @DisplayName("Multiplicador aumenta por racha en partida a partir de 2 aciertos seguidos")
    void testMultiplicadorRachaPartida() {
        // Fácil (1.0) + (3 - 1) * 0.05 = 1.10
        assertEquals(1.1, puntajeService.calcularMultiplicador("FACIL", 0, 3));
    }

    @Test
    @DisplayName("Multiplicador total tiene un tope máximo estricto de x2.0 según relevamiento")
    void testMultiplicadorTopeMaximo() {
        // Difícil (1.4) + 20 días (1.0) + racha partida 10 (0.45) = 2.85 -> capeado a 2.0
        double mult = puntajeService.calcularMultiplicador("DIFICIL", 20, 10);
        assertEquals(2.0, mult);
    }

    @Test
    @DisplayName("Respuesta incorrecta siempre otorga 0 puntos")
    void testPuntosIncorrecto() {
        assertEquals(0, puntajeService.calcularPuntos(false, 1, 5, 1.2));
    }

    @Test
    @DisplayName("Respuesta correcta descuenta pistas y tiempo correctamente")
    void testPuntosCorrectoDescuentos() {
        // Base 1000 - pista 2 (-200) - 10 seg (-50) = 750 * 1.0 = 750
        int pts = puntajeService.calcularPuntos(true, 2, 10, 1.0);
        assertEquals(750, pts);

        // Base 1000 - pista 2 (-200) - pista 3 (-200) - 20 seg (-100) = 500 * 1.2 = 600
        int pts2 = puntajeService.calcularPuntos(true, 3, 20, 1.2);
        assertEquals(600, pts2);
    }

    @Test
    @DisplayName("Respuesta correcta siempre otorga puntos (piso mínimo de 50 pts garantizado)")
    void testPisoMinimoPuntos() {
        // Si tardó 200 segundos y usó todas las pistas (1000 - 400 - 1000 = -400), debe dar al menos 50 base
        int pts = puntajeService.calcularPuntos(true, 3, 200, 1.0);
        assertTrue(pts >= 50, "Debe otorgar al menos el piso mínimo de 50 puntos al acertar");
    }
}
