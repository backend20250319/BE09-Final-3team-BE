package site.petful.advertiserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "site.petful.advertiserservice.service")
public class AdvertiserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvertiserServiceApplication.class, args);
    }

}
