package com.inkwell.post.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {
	@Bean
	TopicExchange exchange() {
		return new TopicExchange("inkwell.exchange");
	}
	
	@Bean
	Queue postQueue() {
		return new Queue("post.published.queue", true);
	}
	
	@Bean
	Binding binding(Queue postQueue, TopicExchange exchange) {
		return BindingBuilder.bind(postQueue).to(exchange).with("post-published");
	}
}
