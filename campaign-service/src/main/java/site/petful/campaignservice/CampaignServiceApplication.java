package site.petful.campaignservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import site.petful.campaignservice.config.FeignConfig;

@EnableJpaAuditing
@SpringBootApplication
@EnableFeignClients(defaultConfiguration = FeignConfig.class)
public class CampaignServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampaignServiceApplication.class, args);
	}

}
