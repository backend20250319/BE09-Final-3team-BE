package site.petful.healthservice.activity.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import site.petful.healthservice.activity.dto.PetResponse;

@FeignClient(name = "pet-service", url = "${pet-service.url}")
public interface PetServiceClient {
    
    @GetMapping("/api/v1/pet-service/pets/{petNo}")
    PetResponse getPetById(@PathVariable("petNo") Long petNo);
}
