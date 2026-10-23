package com.cine.ms_notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_RESERVAS = "reservas.exchange";
    public static final String EXCHANGE_DLX = "reservas.dlx";
    public static final String QUEUE_RESERVA_CONFIRMADA = "notificaciones.reserva.confirmada";
    public static final String QUEUE_RESERVA_CONFIRMADA_DLQ = "notificaciones.reserva.confirmada.dlq";
    public static final String ROUTING_KEY_RESERVA_CONFIRMADA = "reserva.confirmada";
    public static final String ROUTING_KEY_FALLIDA = "fallida";

    @Bean
    public TopicExchange reservasExchange() {
        return new TopicExchange(EXCHANGE_RESERVAS, true, false);
    }

    @Bean
    public DirectExchange reservasDlxExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue notificacionesReservaConfirmadaQueue() {
        return QueueBuilder.durable(QUEUE_RESERVA_CONFIRMADA)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_FALLIDA)
                .build();
    }

    @Bean
    public Queue notificacionesReservaConfirmadaDlq() {
        return QueueBuilder.durable(QUEUE_RESERVA_CONFIRMADA_DLQ).build();
    }

    @Bean
    public Binding notificacionesReservaConfirmadaBinding(
            @Qualifier("notificacionesReservaConfirmadaQueue") Queue notificacionesReservaConfirmadaQueue,
            @Qualifier("reservasExchange") TopicExchange reservasExchange) {
        return BindingBuilder.bind(notificacionesReservaConfirmadaQueue)
                .to(reservasExchange)
                .with(ROUTING_KEY_RESERVA_CONFIRMADA);
    }

    @Bean
    public Binding notificacionesReservaConfirmadaDlqBinding(
            @Qualifier("notificacionesReservaConfirmadaDlq") Queue notificacionesReservaConfirmadaDlq,
            @Qualifier("reservasDlxExchange") DirectExchange reservasDlxExchange) {
        return BindingBuilder.bind(notificacionesReservaConfirmadaDlq)
                .to(reservasDlxExchange)
                .with(ROUTING_KEY_FALLIDA);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
