package site.petful.snsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SnsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnsServiceApplication.class, args);
    }
}
