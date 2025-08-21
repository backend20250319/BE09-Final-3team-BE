// RabbitConfig.java
package site.petful.notificationservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.ArrayList;
import java.util.List;

@EnableRabbit
@Configuration
@EnableConfigurationProperties(MessagingProps.class)
public class RabbitConfig {

    private final MessagingProps props;
    public RabbitConfig(MessagingProps props) { this.props = props; }

    @Bean
    TopicExchange notiExchange() {
        return ExchangeBuilder.topicExchange(props.getExchange())
                .durable(true)
                .build();
    }

    @Bean
    Queue notiQueue() {
        return QueueBuilder.durable(props.getQueue())
                .build(); // durable=true, autoDelete=false, exclusive=false
    }

    @Bean
    Declarables bindings(TopicExchange ex, Queue q) {
        List<Declarable> ds = new ArrayList<>();
        for (String raw : props.getKeys()) {
            String key = raw == null ? "" : raw.trim();
            if (!key.isEmpty()) ds.add(BindingBuilder.bind(q).to(ex).with(key));
        }
        return new Declarables(ds);
    }

    @Bean
    AmqpAdmin amqpAdmin(ConnectionFactory cf) {
        RabbitAdmin admin = new RabbitAdmin(cf);
        admin.setAutoStartup(true);
        return admin;
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
        f.setMissingQueuesFatal(false);
        return f;
    }
}
