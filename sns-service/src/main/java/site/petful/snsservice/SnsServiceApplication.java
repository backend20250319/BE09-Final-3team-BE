package site.petful.snsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@SpringBootApplication
@EnableScheduling
public class SnsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnsServiceApplication.class, args);
    }
}
