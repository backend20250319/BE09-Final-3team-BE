package site.petful.notificationservice.messaging;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@EnableRabbit
@Configuration
public class RabbitConfig {
    @Value("${app.messaging.exchange}")
    private String exchangeName;
    @Value("${app.messaging.queue}")
    private String queueName;
    @Value("${app.messaging.keys:}")
    private List<String> routingKeys;

    @Bean
    TopicExchange notiExchange() {return new TopicExchange(exchangeName,true,false);}
    @Bean
    Queue notiQueue() {return QueueBuilder.durable(queueName).build();}

    @Bean
    Declarables bindings(TopicExchange ex, Queue q) {
        List<Declarable> ds = new ArrayList<>();
        for (String raw : routingKeys) {
            String key = raw.trim();
            if (!key.isEmpty()) ds.add(BindingBuilder.bind(q).to(ex).with(key));
        }
        return new Declarables(ds);
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(conv);
        return t;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(conv);
        f.setConcurrentConsumers(2);
        f.setMaxConcurrentConsumers(8);
        return f;
    }
}
