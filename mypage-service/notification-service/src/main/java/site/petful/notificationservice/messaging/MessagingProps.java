// MessagingProps.java
package site.petful.notificationservice.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProps {
    private String exchange;
    private String queue;
    private List<String> keys = new ArrayList<>();

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getQueue() { return queue; }
    public void setQueue(String queue) { this.queue = queue; }
    public List<String> getKeys() { return keys; }
    public void setKeys(List<String> keys) { this.keys = keys; }
}
