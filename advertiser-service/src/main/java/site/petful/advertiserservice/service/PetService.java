package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.client.PetFeignClient;
import site.petful.advertiserservice.client.SnSFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.campaign.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {

    private final PetFeignClient petFeignClient;
    private final SnSFeignClient snSFeignClient;

    // 1. 펫스타 전체 목록 조회
    public List<PetResponse> geAllPetstars() {
        ApiResponse<List<PetResponse>> response = petFeignClient.getAllPetStars();
        return response.getData();
    }

    // 1-2. 펫스타별 인스타 media 목록 조회
    public List<InstagramMediaDto> geAllPetstarsMedia() {
        List<PetResponse> response = petFeignClient.getAllPetStars().getData();

        List<InstagramMediaDto> allMedias = new ArrayList<>();

        for (PetResponse pet : response) {
            Long instagramId = pet.getSnsProfileNo();
            log.info("instagramId:{}", instagramId);
            if (instagramId != null) {
                ApiResponse<List<InstagramMediaDto>> mediaResponse = snSFeignClient.getMedias(instagramId);
                log.info("mediaResponse:{}", mediaResponse.getData());
                if (mediaResponse != null && mediaResponse.getData() != null) {
                    allMedias.addAll(mediaResponse.getData());
                    log.info("allMedias:{}", allMedias);
                }
            }
        }

        return allMedias;
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

