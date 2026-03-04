package info.jemsit.media_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static info.jemsit.common.data.constants.RabbitMQConstants.*;


@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue eventQueue() {
        return new Queue(MEDIA_QUEUE, true);
    }

    @Bean
    public TopicExchange mediaExchange() {
        return new TopicExchange(MEDIA_EXCHANGE);
    }

    @Bean
    public Binding mediaBinding(Queue eventQueue, TopicExchange mediaExchange) {
        return BindingBuilder.bind(eventQueue).to(mediaExchange).with(MEDIA_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate (ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
