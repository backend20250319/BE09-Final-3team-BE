package site.petful.notificationservice.webpush;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vapid")
@Getter
@Setter
public class VapidProps {
    private String publicKey;
    private String privateKey;
    private String subject; // mailto:...
}
