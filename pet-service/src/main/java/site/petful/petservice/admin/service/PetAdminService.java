package site.petful.petservice.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.petservice.client.UserClient;
import site.petful.petservice.common.ApiResponse;
import site.petful.petservice.dto.PetStarResponse;
import site.petful.petservice.dto.SimpleProfileResponse;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.entity.PetStarStatus;
import site.petful.petservice.repository.PetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PetAdminService {
    private final PetRepository petRepository;
    private final UserClient userClient;
    
    // PetStar 목록 조회 (관리자용) - 모든 신청을 보여주되 사용자 정보가 없는 경우 적절히 처리
    public Page<PetStarResponse> getPetStarApplications(Pageable pageable) {
        log.info("펫스타 신청 목록 조회 시작");
        
        // 모든 PENDING 상태의 펫스타 신청 목록 조회
        Page<Pet> pets = petRepository.findByPetStarStatus(PetStarStatus.PENDING, pageable);
        log.info("PENDING 상태 펫스타 신청 수: {}", pets.getTotalElements());

        // 사용자 정보 조회 시도하여 PetStarResponse 생성
        return pets.map(pet -> {
            // userNo가 null이거나 0인 경우
            if (pet.getUserNo() == null || pet.getUserNo() <= 0) {
                log.warn("유효하지 않은 userNo - petNo: {}, userNo: {}", pet.getSnsProfileNo(), pet.getUserNo());
                return new PetStarResponse(
                        pet.getPetNo(),
                        pet.getSnsProfileNo(),
                        pet.getName(),
                        pet.getAge(),
                        "사용자 정보 없음",
                        "사용자 정보 없음",
                        "사용자 정보 없음",
                        pet.getGender(),
                        pet.getType()
                );
            }

            try {
                log.info("사용자 정보 조회 시도 - petNo: {}, userNo: {}", pet.getSnsProfileNo(), pet.getUserNo());
                ApiResponse<SimpleProfileResponse> data = userClient.getUserBrief(pet.getUserNo());
                SimpleProfileResponse user = data.getData();
                log.info("사용자 정보 조회 성공 - userNo: {}, name: {}", user.getId(), user.getNickname());
                log.info(data.toString());
                return new PetStarResponse(
                        pet.getPetNo(),
                        pet.getSnsProfileNo(),
                        pet.getName(),
                        pet.getAge(),
                        user.getNickname(),
                        user.getPhone(),
                        user.getEmil(),
                        pet.getType(),
                        pet.getGender()
                );
            } catch (Exception e) {
                log.error("UserClient 호출 실패 - petNo: {}, userNo: {}, error: {}", 
                         pet.getSnsProfileNo(), pet.getUserNo(), e.getMessage());
                
                // 404 오류인 경우 (사용자가 존재하지 않음)
                if (e.getMessage() != null && e.getMessage().contains("404")) {
                    log.warn("존재하지 않는 사용자 - petNo: {}, userNo: {}", pet.getSnsProfileNo(), pet.getUserNo());
                    return new PetStarResponse(
                            pet.getPetNo(),
                            pet.getSnsProfileNo(),
                            pet.getName(),
                            pet.getAge(),
                            "사용자 ID " + pet.getUserNo() + " 없음",
                            "사용자 ID " + pet.getUserNo() + " 없음",
                            "사용자 ID " + pet.getUserNo() + " 없음",
                            pet.getType(),
                            pet.getGender()
                    );
                }
                
                // 기타 오류인 경우 (네트워크 오류 등)
                log.error("사용자 정보 조회 중 예상치 못한 오류 - petNo: {}, userNo: {}, error: {}", 
                         pet.getSnsProfileNo(), pet.getUserNo(), e.getMessage());
                return new PetStarResponse(
                        pet.getPetNo(),
                        pet.getSnsProfileNo(),
                        pet.getName(),
                        pet.getAge(),
                        "사용자 정보 조회 실패",
                        "사용자 정보 조회 실패",
                        "사용자 정보 조회 실패",
                        pet.getGender(),
                        pet.getType()
                );
            }
        });
    }

    // PetStar 승인 (관리자용)
    @Transactional
    public void approvePetStar(Long petNo) {

        Pet  pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (pet.getPetStarStatus() != PetStarStatus.PENDING) {
            throw new IllegalArgumentException("승인 대기 중인 PetStar 신청이 아닙니다.");
        }

        pet.setPetStarStatus(PetStarStatus.ACTIVE);
        pet.setIsPetStar(true);
        petRepository.save(pet);
    }

    // PetStar 거절 (관리자용)
    @Transactional
    public void rejectPetStar(Long petNo, String reason) {
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (pet.getPetStarStatus() != PetStarStatus.PENDING) {
            throw new IllegalArgumentException("승인 대기 중인 PetStar 신청이 아닙니다.");
        }
        pet.setRejectReason(reason);

        pet.setPetStarStatus(PetStarStatus.REJECTED);
        petRepository.save(pet);
    }

}
