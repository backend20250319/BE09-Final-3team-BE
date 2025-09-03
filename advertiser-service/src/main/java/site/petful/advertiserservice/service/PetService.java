package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.client.PetFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.campaign.PetResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetFeignClient petFeignClient;

    // 1. 펫스타 전체 목록 조회
    public List<PetResponse> geAllPetstars() {
        ApiResponse<List<PetResponse>> response = petFeignClient.getAllPetStars();
        return response.getData();
    }
}

