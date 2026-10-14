package com.cine.ms_notificaciones.services;

import com.cine.ms_notificaciones.dto.ReservaConfirmadaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificacionService {

    public void enviarConfirmacion(ReservaConfirmadaMessage message) {
        log.info("Enviando confirmacion de reserva -> reservaId={}, usuarioId={}, email={}, funcionId={}, asientos={}, total={}",
                message.getReservaId(),
                message.getUsuarioId(),
                message.getEmailUsuario(),
                message.getFuncionId(),
                message.getAsientos(),
                message.getTotal());

        // TODO: implementar el envio real del correo de confirmacion al emailUsuario.
    }

}
