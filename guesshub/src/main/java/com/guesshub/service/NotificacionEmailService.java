package com.guesshub.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificacionEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${guesshub.mail.remitente:notificaciones@guesshub.com}")
    private String remitente;

    /**
     * Notificación cuando el usuario está por perder la racha (último día para jugar)
     */
    public void notificarRachaEnRiesgo(String email, String displayName, int rachaDias) {
        String asunto = "🔥 ¡Tu racha de " + rachaDias + " días en GuessHub está en riesgo!";
        String contenido = """
                <h2>¡Hola %s!</h2>
                <p>Notamos que todavía no jugaste el <strong>Desafío de Hoy</strong>.</p>
                <p>Tu racha actual es de <strong>%d días consecutivos</strong>. Si no jugás antes de medianoche, la perderás (a menos que uses un comodín para congelarla).</p>
                <p><a href="http://localhost:8080/" style="background:#4CAF50;color:#fff;padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:bold;">Jugar Desafío Ahora</a></p>
                <p>¡El equipo de GuessHub!</p>
                """.formatted(displayName, rachaDias);

        enviarOsimular(email, asunto, contenido);
    }

    /**
     * Notificación cuando el usuario es desplazado del ranking (Top 10 / Top 3 / Top 1)
     */
    public void notificarDesplazamientoRanking(String email, String displayName, int puestoAnterior, int puestoActual) {
        String asunto = "⚡ ¡Alguien te superó en el Ranking de GuessHub!";
        String contenido = """
                <h2>¡Atención %s!</h2>
                <p>Otro jugador acaba de superar tu puntuación.</p>
                <p>Estabas en el puesto <strong>#%d</strong> y ahora te encontrás en el puesto <strong>#%d</strong>.</p>
                <p>¡Jugá una partida para recuperar tu lugar en la cima!</p>
                <p><a href="http://localhost:8080/ranking" style="background:#FFA726;color:#fff;padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:bold;">Ver Tabla de Posiciones</a></p>
                """.formatted(displayName, puestoAnterior, puestoActual);

        enviarOsimular(email, asunto, contenido);
    }

    /**
     * Notificación por inactividad prolongada
     */
    public void notificarInactividad(String email, String displayName) {
        String asunto = "👋 ¡Te extrañamos en GuessHub!";
        String contenido = """
                <h2>¡Hola %s!</h2>
                <p>Hace varios días que no jugás en GuessHub.</p>
                <p>Tenemos nuevos desafíos diarios en <strong>Películas, Música y Deportes</strong> esperando por vos.</p>
                <p><a href="http://localhost:8080/" style="background:#4CAF50;color:#fff;padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:bold;">Volver a Jugar</a></p>
                """.formatted(displayName);

        enviarOsimular(email, asunto, contenido);
    }

    /**
     * Notificación para restablecer contraseña con código de seguridad
     */
    public void notificarRecuperacionPassword(String email, String displayName, String token) {
        log.info("================================================================================");
        log.info("🔐 [GUESS-HUB RECUPERACIÓN] CÓDIGO DE SEGURIDAD PARA {}: [{}]", email, token);
        log.info("================================================================================");

        String asunto = "🔐 Código de recuperación de contraseña — GuessHub";
        String contenido = """
                <h2>¡Hola %s!</h2>
                <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta de GuessHub.</p>
                <p>Tu código de seguridad es:</p>
                <div style="background:#E8F5E9;border:2px dashed #4CAF50;padding:12px 24px;border-radius:8px;display:inline-block;font-size:24px;font-weight:bold;letter-spacing:4px;color:#2E7D32;margin:10px 0;">
                    %s
                </div>
                <p>Este código expira en <strong>15 minutos</strong>. Si no solicitaste este cambio, podés ignorar este mensaje.</p>
                <p>¡El equipo de GuessHub!</p>
                """.formatted(displayName, token);

        enviarOsimular(email, asunto, contenido);
    }

    /**
     * Notificación al agregar nuevos juegos o categorías
     */
    public void notificarNuevoJuego(String email, String displayName, String nombreJuego) {
        String asunto = "🎉 ¡Nuevo minijuego disponible en GuessHub: " + nombreJuego + "!";
        String contenido = """
                <h2>¡Novedad para %s!</h2>
                <p>Acabamos de lanzar un nuevo minijuego: <strong>%s</strong>.</p>
                <p>¡Sé el primero en probarlo y liderar el ranking!</p>
                <p><a href="http://localhost:8080/game" style="background:#4CAF50;color:#fff;padding:10px 20px;border-radius:8px;text-decoration:none;font-weight:bold;">Probar Minijuego</a></p>
                """.formatted(displayName, nombreJuego);

        enviarOsimular(email, asunto, contenido);
    }

    private void enviarOsimular(String destinatario, String asunto, String html) {
        if (destinatario == null || !destinatario.contains("@")) {
            log.warn("Destinatario de email inválido: {}", destinatario);
            return;
        }

        // Si hay un mailSender y credenciales SMTP cargadas, intentar envío real
        if (mailSender != null && mailUsername != null && !mailUsername.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                String from = (remitente != null && !remitente.isBlank()) ? remitente : mailUsername;
                helper.setFrom(from);
                helper.setTo(destinatario);
                helper.setSubject(asunto);
                helper.setText(html, true);
                mailSender.send(mimeMessage);
                log.info("Email real enviado exitosamente a: {} | Asunto: {}", destinatario, asunto);
                return;
            } catch (Exception e) {
                log.warn("Error al enviar email real a {}: {}. Registrando en log.", destinatario, e.getMessage());
            }
        }

        // Modo simulación (cuando no hay servidor SMTP configurado)
        log.info("[SIMULACIÓN EMAIL] Destinatario: {} | Asunto: {}", destinatario, asunto);
    }
}
