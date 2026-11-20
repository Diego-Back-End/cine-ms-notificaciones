package com.cine.ms_notificaciones.listeners;

import com.cine.ms_notificaciones.dto.ReservaFallidaMessage;
import com.cine.ms_notificaciones.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaFallidaListener {

    private final NotificacionService notificacionService;

    @RabbitListener(containerFactory = "rabbitListenerContainerFactoryFallida", queues = "notificaciones.reserva.fallida")
    public void onReservaFallida(ReservaFallidaMessage message) {
        log.info("Mensaje de reserva fallida recibido: reservaId={}", message.getReservaId());
        try {
            notificacionService.enviarFallida(message);
        } catch (Exception e) {
            log.error("Error al enviar fallida de reserva: reservaId={}, motivo={}",
                    message.getReservaId(), e.getMessage(), e);
            throw e;
        }
    }

}
