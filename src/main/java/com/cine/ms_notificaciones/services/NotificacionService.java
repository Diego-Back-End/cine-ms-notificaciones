package com.cine.ms_notificaciones.services;

import com.cine.ms_notificaciones.dto.ReservaConfirmadaMessage;
import com.cine.ms_notificaciones.dto.ReservaFallidaMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String mailFrom;

    public void enviarConfirmacion(ReservaConfirmadaMessage message) {
        String asunto = "Confirmación de tu reserva #" + message.getReservaId();
        String cuerpo = construirCuerpo(message);

        if (!mailEnabled) {
            log.info("Correo deshabilitado (app.mail.enabled=false). Simulando envío -> para={}, asunto={}, cuerpo=\n{}",
                    message.getEmailUsuario(), asunto, cuerpo);
            return;
        }

        SimpleMailMessage correo = new SimpleMailMessage();
        correo.setTo(message.getEmailUsuario());
        if (mailFrom != null && !mailFrom.isBlank()) {
            correo.setFrom(mailFrom);
        }
        correo.setSubject(asunto);
        correo.setText(cuerpo);

        mailSender.send(correo);
        log.info("Correo de confirmación enviado a {} (reservaId={})",
                message.getEmailUsuario(), message.getReservaId());
    }

    public void enviarFallida(ReservaFallidaMessage message) {
        String email = message.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("Correo de compra no completada omitido: email vacío o nulo (reservaId={})",
                    message.getReservaId());
            return;
        }

        String asunto = "Tu compra no se pudo completar";
        String motivo = message.getMotivo();
        String explicacion = switch (motivo == null ? "" : motivo) {
            case "CANCELADA_POR_USUARIO" -> "cancelaste la compra desde la pantalla de pago";
            case "EXPIRADA" -> "no se completó el pago a tiempo";
            default -> "la compra no pudo completarse";
        };
        String cuerpo = """
                <html>
                <body>
                <p>La reserva N° %s no se completó porque %s. Los asientos fueron liberados.</p>
                </body>
                </html>
                """.formatted(message.getReservaId(), explicacion);

        if (!mailEnabled) {
            log.info("Correo deshabilitado (app.mail.enabled=false). Simulando envío -> para={}, asunto={}, cuerpo=\n{}",
                    email, asunto, cuerpo);
            return;
        }

        try {
            MimeMessage correo = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(correo, "UTF-8");
            helper.setTo(email);
            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }
            helper.setSubject(asunto);
            helper.setText(cuerpo, true);
            mailSender.send(correo);
        } catch (MessagingException e) {
            throw new IllegalStateException("Error al construir el correo de compra no completada (reservaId="
                    + message.getReservaId() + ")", e);
        }

        log.info("Correo de compra no completada enviado a {} (reservaId={})",
                email, message.getReservaId());
    }

    private String construirCuerpo(ReservaConfirmadaMessage message) {
        String asientos = message.getAsientos() == null || message.getAsientos().isEmpty()
                ? "(sin asientos)"
                : String.join(", ", message.getAsientos());

        return """
                ¡Gracias por tu compra en CineCloud!

                Tu reserva ha sido confirmada con los siguientes datos:

                Reserva:   #%s
                Función:   %s
                Asientos:  %s
                Total:     $%s

                Te esperamos. ¡Disfruta la función!
                """.formatted(
                message.getReservaId(),
                message.getFuncionId(),
                asientos,
                message.getTotal());
    }

}
