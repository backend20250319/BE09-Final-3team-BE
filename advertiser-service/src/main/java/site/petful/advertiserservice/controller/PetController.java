package site.petful.advertiserservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.dto.campaign.PetResponse;
import site.petful.advertiserservice.service.PetService;

import java.util.List;

@RestController
@RequestMapping("/pet")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // 1. 펫스타 전체 목록 조회
    @GetMapping("/petstars")
    public ResponseEntity<ApiResponse<?>> getAllPetstars() {
        List<PetResponse> response = petService.geAllPetstars();
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}