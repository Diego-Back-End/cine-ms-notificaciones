package com.cine.ms_notificaciones.services;

import com.cine.ms_notificaciones.dto.ReservaConfirmadaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
