package site.petful.petservice.admin.service;

import lombok.RequiredArgsConstructor;
import site.petful.petservice.admin.client.UserResponse;
import site.petful.petservice.client.UserClient;
import site.petful.petservice.dto.PetStarResponse;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.entity.PetStarStatus;
import site.petful.petservice.repository.PetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetAdminService {
    private final PetRepository petRepository;
    private final UserClient userClient;
    // PetStar 목록 조회 (관리자용)
    public Page<PetStarResponse> getPetStarApplications(Pageable pageable) {
        Page<Pet> pets = petRepository.findByPetStarStatus(PetStarStatus.PENDING, pageable);

        // 현재는 기본 정보만 반환
        return pets.map(pet -> {
            UserResponse user = userClient.getUserById(pet.getUserNo());
            return new PetStarResponse(
                    pet.getSnsProfileNo(),
                    pet.getName(),
                    user.getName(),
                    user.getPhone(),
                    user.getEmail()
            );
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
    public void rejectPetStar(Long petNo) {
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (pet.getPetStarStatus() != PetStarStatus.PENDING) {
            throw new IllegalArgumentException("승인 대기 중인 PetStar 신청이 아닙니다.");
        }

        pet.setPetStarStatus(PetStarStatus.REJECTED);
        petRepository.save(pet);
    }

}
