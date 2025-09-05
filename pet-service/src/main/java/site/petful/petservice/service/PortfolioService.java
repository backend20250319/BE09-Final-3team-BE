package site.petful.petservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.petservice.dto.PortfolioRequest;
import site.petful.petservice.dto.PortfolioResponse;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.entity.Portfolio;
import site.petful.petservice.repository.PetRepository;
import site.petful.petservice.repository.PortfolioRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PetRepository petRepository;

        // 포트폴리오 생성 또는 업데이트
    @Transactional
    public PortfolioResponse createPortfolio(Long petNo, Long userNo, PortfolioRequest request) {
        // 펫 존재 여부 및 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물의 포트폴리오를 생성할 권한이 없습니다.");
        }

        // 기존 포트폴리오가 존재하는지 확인
        Optional<Portfolio> existingPortfolio = portfolioRepository.findByPetNo(petNo);
        
        Portfolio savedPortfolio;
        if (existingPortfolio.isPresent()) {
            // 기존 포트폴리오가 있으면 업데이트
            Portfolio portfolio = existingPortfolio.get();
            
            // 소유권 확인 (이미 위에서 확인했으므로 중복 확인 제거)
            // if (!portfolio.getPet().getUserNo().equals(userNo)) {
            //     throw new IllegalArgumentException("해당 포트폴리오를 수정할 권한이 없습니다.");
            // }
            
            // 포트폴리오 정보 업데이트
            portfolio.setContent(request.getContent());
            portfolio.setCost(request.getCost());
            portfolio.setContact(request.getContact());
            portfolio.setIsSaved(request.getIsSaved() != null ? request.getIsSaved() : portfolio.getIsSaved());
            portfolio.setPersonality(request.getPersonality());
            
            // 일반 save() 사용
            savedPortfolio = portfolioRepository.save(portfolio);
        } else {
            // 새로운 포트폴리오 생성
            Portfolio portfolio = Portfolio.builder()
                    .petNo(petNo)
                    .content(request.getContent())
                    .cost(request.getCost())
                    .contact(request.getContact())
                    .isSaved(request.getIsSaved() != null ? request.getIsSaved() : false)
                    .personality(request.getPersonality())
                    .build();
            
            // 일반 save() 사용
            savedPortfolio = portfolioRepository.save(portfolio);
        }

        return toPortfolioResponse(savedPortfolio);
    }

    // 포트폴리오 조회
    public PortfolioResponse getPortfolio(Long petNo, Long userNo) {
        Portfolio portfolio = portfolioRepository.findByPetNo(petNo)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다: " + petNo));

        // 펫 정보로 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));
        
        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 포트폴리오를 조회할 권한이 없습니다.");
        }

        return toPortfolioResponse(portfolio);
    }

    public PortfolioResponse getPortfolioExternal(Long petNo) {

        Portfolio portfolio = portfolioRepository.findByPetNo(petNo)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다: " + petNo));

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        return toPortfolioResponse(portfolio);
    }

    // 사용자의 모든 포트폴리오 조회
    public List<PortfolioResponse> getPortfoliosByUser(Long userNo) {
        List<Portfolio> portfolios = portfolioRepository.findByUserNo(userNo);
        return portfolios.stream()
                .map(this::toPortfolioResponse)
                .collect(Collectors.toList());
    }

    // 포트폴리오 수정
    @Transactional
    public PortfolioResponse updatePortfolio(Long petNo, Long userNo, PortfolioRequest request) {
        Portfolio portfolio = portfolioRepository.findByPetNo(petNo)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다: " + petNo));

        // 펫 정보로 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));
        
        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 포트폴리오를 수정할 권한이 없습니다.");
        }

                 // 포트폴리오 정보 업데이트
         portfolio.setContent(request.getContent());
         portfolio.setCost(request.getCost());
         portfolio.setContact(request.getContact());
         portfolio.setIsSaved(request.getIsSaved() != null ? request.getIsSaved() : portfolio.getIsSaved());
         portfolio.setPersonality(request.getPersonality());



        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return toPortfolioResponse(updatedPortfolio);
    }

    // 포트폴리오 삭제
    @Transactional
    public void deletePortfolio(Long petNo, Long userNo) {
        Portfolio portfolio = portfolioRepository.findByPetNo(petNo)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다: " + petNo));

        // 펫 정보로 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));
        
        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 포트폴리오를 삭제할 권한이 없습니다.");
        }


        
        // 포트폴리오 삭제
        portfolioRepository.delete(portfolio);
    }

    // 임시저장 포트폴리오 조회
    public List<PortfolioResponse> getSavedPortfolios(Long userNo) {
        List<Portfolio> savedPortfolios = portfolioRepository.findByIsSavedTrue();
        return savedPortfolios.stream()
                .filter(portfolio -> {
                    Pet pet = petRepository.findById(portfolio.getPetNo())
                            .orElse(null);
                    return pet != null && pet.getUserNo().equals(userNo);
                })
                .map(this::toPortfolioResponse)
                .collect(Collectors.toList());
    }



    // DTO 변환 메서드
    private PortfolioResponse toPortfolioResponse(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .petNo(portfolio.getPetNo())
                .content(portfolio.getContent())
                .cost(portfolio.getCost())
                .contact(portfolio.getContact())
                .isSaved(portfolio.getIsSaved())
                .personality(portfolio.getPersonality())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}
