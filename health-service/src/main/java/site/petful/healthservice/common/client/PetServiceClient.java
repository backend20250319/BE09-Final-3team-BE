package site.petful.healthservice.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.dto.PetResponse;

import java.util.List;

@FeignClient(name = "pet-service", path = "/")
public interface PetServiceClient {
    
    @GetMapping("/pets/{petNo}")
    ApiResponse<PetResponse> getPet(@PathVariable Long petNo);
    
    @GetMapping("/pets/jwt")
    ApiResponse<List<PetResponse>> getPets(@RequestHeader("X-User-No") Long userNo);
}
