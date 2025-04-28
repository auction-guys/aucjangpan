package com.fifteen.auction.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fifteen.auction.domain.auction.dto.event.BidRequestEvent;
import com.fifteen.auction.domain.auction.dto.event.BuyNowRequestEvent;
import com.fifteen.auction.domain.auction.infrastructure.QueueKeyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    public static final int QUEUE_COUNT = 16;
    public static final String BID_EXCHANGE = "bid.exchange";
    public static final String BID_QUEUE_PREFIX = "bid.queue.";
    public static final String BID_ROUTING_KEY_PREFIX = "bid.routing.";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static final String TYPE_ID_HEADER_KEY = "__TypeId__";
    public static final String TYPE_ID_BID_REQUEST_EVENT = "BidRequestEvent";
    public static final String TYPE_ID_BUY_NOW_REQUEST_EVENT = "BuyNowRequestEvent";

    private static final List<String> queueNames = new ArrayList<>();

    public List<String> getQueueNames() {
        return queueNames;
    }

    @Bean
    public static BeanFactoryPostProcessor dynamicQueueRegistrar() {
        return beanFactory -> {
            DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
            DirectExchange directExchange = new DirectExchange(BID_EXCHANGE);
            factory.registerSingleton("directExchange", directExchange);

            for (int i = 0; i < QUEUE_COUNT; i++) {
                String queueName = BID_QUEUE_PREFIX + i;
                Queue queue = new Queue(queueName, false);
                factory.registerSingleton("queue" + i, queue);
                queueNames.add(queueName);

                Binding bind = BindingBuilder.bind(queue)
                        .to(directExchange)
                        .with(BID_ROUTING_KEY_PREFIX + i);
                factory.registerSingleton("bind" + i, bind);
            }
        };
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        return factory;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        om.registerModules(
                new JavaTimeModule()
                        .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_FORMATTER))
                        .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_FORMATTER))
        );
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(om);

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.ofEntries(
                        Map.entry(TYPE_ID_BID_REQUEST_EVENT, BidRequestEvent.class),
                        Map.entry(TYPE_ID_BUY_NOW_REQUEST_EVENT, BuyNowRequestEvent.class)
                )
        );
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public QueueKeyResolver queueKeyResolver() {
        return new QueueKeyResolver(QUEUE_COUNT);
    }
}
