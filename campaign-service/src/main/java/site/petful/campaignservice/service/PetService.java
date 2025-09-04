package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.client.PetFeignClient;
import site.petful.campaignservice.dto.HistoryImageInfo;
import site.petful.campaignservice.dto.HistoryResponse;
import site.petful.campaignservice.dto.pet.PetResponse;
import site.petful.campaignservice.dto.pet.PortfolioResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetFeignClient petFeignClient;

    // 1. 사용자별 펫 조회
    public List<PetResponse> getPets(Long userNo) {
        return petFeignClient.getPetsExternal(userNo).getData();
    }

    // 2. 포트폴리오 조회
    public PortfolioResponse getPortfolio(Long petNo) {
        return petFeignClient.getPortfolioExternal(petNo).getData();
    }

    // 3. 활동이력 조회
    public List<HistoryResponse> getHistory(Long petNo) {
        return petFeignClient.getHistoriesExternal(petNo).getData();
    }

    // 4. 활동이력 이미지 조회
    public List<HistoryImageInfo> getImages(Long petNo) {
        return petFeignClient.getHistoryImagesExternal(petNo).getData();
    }
}
