package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.client.PetFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.campaign.HistoryImageInfo;
import site.petful.advertiserservice.dto.campaign.HistoryResponse;
import site.petful.advertiserservice.dto.campaign.PetResponse;
import site.petful.advertiserservice.dto.campaign.PortfolioResponse;

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

    // 2. 반려동물 상세 조회
    public PetResponse getPet(Long petNo) {
        ApiResponse<PetResponse> response = petFeignClient.getPet(petNo);
        return response.getData();
    }

    // 3. 포트폴리오 조회
    public PortfolioResponse getPortfolio(Long petNo) {
        return petFeignClient.getPortfolioExternal(petNo).getData();
    }

    // 4. 활동이력 조회
    public List<HistoryResponse> getHistory(Long petNo) {
        return petFeignClient.getHistoriesExternal(petNo).getData();
    }
}

