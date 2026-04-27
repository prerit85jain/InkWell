package com.inkwell.notification.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAsync
public class AppConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.comment-events}")
    private String commentQueue;

    @Value("${app.rabbitmq.queue.post-events}")
    private String postQueue;

    @Value("${app.rabbitmq.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.rabbitmq.retry.initial-interval:1000}")
    private long initialInterval;

    @Value("${app.rabbitmq.retry.multiplier:2.0}")
    private double multiplier;

    @Value("${app.rabbitmq.retry.max-interval:10000}")
    private long maxInterval;

    // ── Dead Letter Exchanges & Queues ────────────────────────

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(exchange + ".dlx", true, false);
    }

    @Bean
    public Queue commentEventDlq() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchange);
        args.put("x-dead-letter-routing-key", "comment.added.dlq");
        return new Queue(commentQueue + ".dlq", true, false, true, args);
    }

    @Bean
    public Queue postEventDlq() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchange);
        args.put("x-dead-letter-routing-key", "post.published.dlq");
        return new Queue(postQueue + ".dlq", true, false, true, args);
    }

    @Bean
    public Binding commentDlqBinding() {
        return BindingBuilder.bind(commentEventDlq())
                .to(deadLetterExchange())
                .with("comment.added.dlq");
    }

    @Bean
    public Binding postDlqBinding() {
        return BindingBuilder.bind(postEventDlq())
                .to(deadLetterExchange())
                .with("post.published.dlq");
    }

    // ── Main Queues with DLQ ────────────────────────────────

    @Bean
    public TopicExchange inkwellExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue commentEventQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchange + ".dlx");
        args.put("x-dead-letter-routing-key", "comment.added.dlq");
        return new Queue(commentQueue, true, false, true, args);
    }

    @Bean
    public Queue postEventQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchange + ".dlx");
        args.put("x-dead-letter-routing-key", "post.published.dlq");
        return new Queue(postQueue, true, false, true, args);
    }

    @Bean
    public Binding commentEventBinding() {
        return BindingBuilder.bind(commentEventQueue())
                .to(inkwellExchange())
                .with("comment.added");
    }

    @Bean
    public Binding postEventBinding() {
        return BindingBuilder.bind(postEventQueue())
                .to(inkwellExchange())
                .with("post.published");
    }

    // ── Retry Configuration ───────────────────────────────

    @Bean
    public ExponentialBackOffPolicy backOffPolicy() {
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(initialInterval);
        policy.setMultiplier(multiplier);
        policy.setMaxInterval(maxInterval);
        return policy;
    }

    @Bean
    public SimpleRetryPolicy retryPolicy() {
        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(maxRetryAttempts);
        return policy;
    }

    // ── Message Converter ───────────────────────────────────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // ── Swagger ──────────────────────────────────────────────────

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("InkWell — Notification Service API")
                        .description("In-app notifications, bulk broadcast, read-state, RabbitMQ event consumer")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
