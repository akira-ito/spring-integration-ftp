package ito.akira.edson.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {
	public static final String QUEUE_ORDERS = "orders-queue";
	public static final String EXCHANGE_ORDERS = "orders-exchange";
	public static final String QUEUE_DEAD_ORDERS = "dead-orders-exchange";

	@Bean
	Queue ordersQueue() {
		return QueueBuilder.durable(QUEUE_ORDERS).build();
	}

	@Bean
	Queue deadLetterQueue() {
		return QueueBuilder.durable(QUEUE_DEAD_ORDERS).build();
	}

	@Bean
	Exchange ordersExchange() {
		return ExchangeBuilder.topicExchange(EXCHANGE_ORDERS).build();
	}

	@Bean
	Binding binding(Queue ordersQueue, TopicExchange ordersExchange) {
		return BindingBuilder.bind(ordersQueue).to(ordersExchange).with(QUEUE_ORDERS);
	}
}
