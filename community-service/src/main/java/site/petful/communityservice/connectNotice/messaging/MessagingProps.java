// MessagingProps.java
package site.petful.communityservice.connectNotice.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.messaging")
@Getter
@Setter
public class MessagingProps {
    private String exchange;
    private String queue;
    private List<String> keys = new ArrayList<>();
}
