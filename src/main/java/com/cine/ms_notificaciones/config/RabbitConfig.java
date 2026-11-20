package com.cine.ms_notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_RESERVAS = "reservas.exchange";
    public static final String EXCHANGE_DLX = "reservas.dlx";
    public static final String QUEUE_RESERVA_CONFIRMADA = "notificaciones.reserva.confirmada";
    public static final String QUEUE_RESERVA_CONFIRMADA_DLQ = "reservas.dlq";
    public static final String QUEUE_RESERVA_FALLIDA = "notificaciones.reserva.fallida";
    public static final String QUEUE_RESERVA_FALLIDA_DLQ = "reservas.dlq.fallida";
    public static final String ROUTING_KEY_RESERVA_CONFIRMADA = "reserva.confirmada";
    public static final String ROUTING_KEY_RESERVA_FALLIDA = "reserva.fallida";

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
                .deadLetterRoutingKey(ROUTING_KEY_RESERVA_CONFIRMADA)
                .build();
    }

    @Bean
    public Queue notificacionesReservaFallidaQueue() {
        return QueueBuilder.durable(QUEUE_RESERVA_FALLIDA)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(ROUTING_KEY_RESERVA_FALLIDA)
                .build();
    }

    @Bean
    public Queue reservasDlq() {
        return QueueBuilder.durable(QUEUE_RESERVA_CONFIRMADA_DLQ).build();
    }

    @Bean
    public Queue reservasDlqFallida() {
        return QueueBuilder.durable(QUEUE_RESERVA_FALLIDA_DLQ).build();
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
    public Binding notificacionesReservaFallidaBinding(
            @Qualifier("notificacionesReservaFallidaQueue") Queue notificacionesReservaFallidaQueue,
            @Qualifier("reservasExchange") TopicExchange reservasExchange) {
        return BindingBuilder.bind(notificacionesReservaFallidaQueue)
                .to(reservasExchange)
                .with(ROUTING_KEY_RESERVA_FALLIDA);
    }

    @Bean
    public Binding reservasDlqBinding(
            @Qualifier("reservasDlq") Queue reservasDlq,
            @Qualifier("reservasDlxExchange") DirectExchange reservasDlxExchange) {
        return BindingBuilder.bind(reservasDlq)
                .to(reservasDlxExchange)
                .with(ROUTING_KEY_RESERVA_CONFIRMADA);
    }

    @Bean
    public Binding reservasDlqFallidaBinding(
            @Qualifier("reservasDlqFallida") Queue reservasDlqFallida,
            @Qualifier("reservasDlxExchange") DirectExchange reservasDlxExchange) {
        return BindingBuilder.bind(reservasDlqFallida)
                .to(reservasDlxExchange)
                .with(ROUTING_KEY_RESERVA_FALLIDA);
    }

    @Bean
    public MessageRecoverer messageRecoverer(ConnectionFactory connectionFactory) {
        RabbitTemplate recovererTemplate = new RabbitTemplate(connectionFactory);
        return new RepublishMessageRecoverer(
                recovererTemplate, EXCHANGE_DLX, ROUTING_KEY_RESERVA_CONFIRMADA);
    }

    @Bean
    public MessageRecoverer messageRecovererFallida(ConnectionFactory connectionFactory) {
        RabbitTemplate recovererTemplate = new RabbitTemplate(connectionFactory);
        return new RepublishMessageRecoverer(
                recovererTemplate, EXCHANGE_DLX, ROUTING_KEY_RESERVA_FALLIDA);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactoryFallida(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter,
            MessageRecoverer messageRecovererFallida) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .recoverer(messageRecovererFallida)
                .build());
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
