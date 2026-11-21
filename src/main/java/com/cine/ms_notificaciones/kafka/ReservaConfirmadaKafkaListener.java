package com.cine.ms_notificaciones.kafka;

import com.cine.ms_notificaciones.dto.ReservaConfirmadaKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Component
public class ReservaConfirmadaKafkaListener {

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(
                (record, exception) -> log.error(
                        "Error al deserializar/procesar mensaje de Kafka: topic={}, partition={}, offset={}, motivo={}",
                        record.topic(), record.partition(), record.offset(), exception.getMessage(), exception),
                new FixedBackOff(0L, 0L));
    }

    @KafkaListener(topics = "reserva-confirmada", groupId = "ms-notificaciones")
    public void onReservaConfirmada(ReservaConfirmadaKafkaMessage message) {
        try {
            log.info("Venta registrada en Kafka: reservaId={}, total={}", message.getReservaId(), message.getTotal());
        } catch (Exception e) {
            log.error("Error al registrar venta desde Kafka: reservaId={}, motivo={}",
                    message.getReservaId(), e.getMessage(), e);
        }
    }

}
