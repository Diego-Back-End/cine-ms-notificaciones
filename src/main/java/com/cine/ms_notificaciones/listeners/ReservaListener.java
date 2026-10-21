package com.cine.ms_notificaciones.listeners;

import com.cine.ms_notificaciones.dto.ReservaConfirmadaMessage;
import com.cine.ms_notificaciones.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaListener {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = "${app.rabbitmq.queue.reserva-confirmada}")
    public void onReservaConfirmada(ReservaConfirmadaMessage message) {
        log.info("Mensaje de reserva confirmada recibido: reservaId={}", message.getReservaId());
        try {
            notificacionService.enviarConfirmacion(message);
        } catch (Exception e) {
            log.error("Error al enviar la notificación de la reserva {}: {}",
                    message.getReservaId(), e.getMessage(), e);
        }
    }

}
