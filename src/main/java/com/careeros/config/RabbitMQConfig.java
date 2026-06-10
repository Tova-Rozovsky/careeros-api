package com.careeros.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for BE-004 async analysis pipeline.
 *
 * Flow:
 * 1. careeros-api publishes job to ANALYSIS_QUEUE
 * 2. Consumer picks it up, calls careeros-ai
 * 3. Result stored in PostgreSQL + Redis
 * 4. Frontend polls GET /api/resumes/{id}/analysis
 *
 * Dead letter queue handles failed jobs after 3 retries.
 */
@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String ANALYSIS_QUEUE = "resume.analysis";
    public static final String ANALYSIS_DLQ   = "resume.analysis.dlq";
    public static final String ANALYSIS_EXCHANGE = "resume.exchange";
    public static final String ANALYSIS_ROUTING_KEY = "resume.analyze";

    // ── Main Queue ────────────────────────────────────────────────────────────
    @Bean
    public Queue analysisQueue() {
        return QueueBuilder.durable(ANALYSIS_QUEUE)
                // Route failed messages to DLQ after exhausting retries
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ANALYSIS_DLQ)
                .build();
    }

    // ── Dead Letter Queue (failed jobs) ───────────────────────────────────────
    @Bean
    public Queue analysisDeadLetterQueue() {
        return QueueBuilder.durable(ANALYSIS_DLQ).build();
    }

    // ── Direct Exchange ───────────────────────────────────────────────────────
    @Bean
    public DirectExchange analysisExchange() {
        return new DirectExchange(ANALYSIS_EXCHANGE);
    }

    // ── Binding: exchange → queue via routing key ─────────────────────────────
    @Bean
    public Binding analysisBinding(Queue analysisQueue, DirectExchange analysisExchange) {
        return BindingBuilder
                .bind(analysisQueue)
                .to(analysisExchange)
                .with(ANALYSIS_ROUTING_KEY);
    }

    // ── JSON message converter ────────────────────────────────────────────────
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ── RabbitTemplate with JSON converter ────────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
