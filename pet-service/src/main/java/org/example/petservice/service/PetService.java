package org.example.petservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.petservice.client.UserClient;
import org.example.petservice.dto.PetRequest;
import org.example.petservice.dto.PetResponse;
import org.example.petservice.dto.PetStarResponse;
import org.example.petservice.entity.Pet;
import org.example.petservice.entity.PetStarStatus;
import org.example.petservice.repository.PetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final UserClient userClient;

    // 반려동물 등록
    @Transactional
    public PetResponse createPet(Long userNo, PetRequest request) {
        // 중복 이름 체크
        if (petRepository.existsByUserNoAndName(userNo, request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 반려동물 이름입니다: " + request.getName());
        }

        Pet pet = Pet.builder()
                .userNo(userNo)
                .name(request.getName())
                .type(request.getType())
                .age(request.getAge())
                .gender(request.getGender())
                .weight(request.getWeight())
                .imageUrl(request.getImageUrl())
                .snsProfileNo(request.getSnsProfileNo())
                .isPetStar(false)
                .petStarStatus(PetStarStatus.NONE)
                .build();

        Pet savedPet = petRepository.save(pet);
        return toPetResponse(savedPet);
    }

    // 반려동물 목록 조회
    public List<PetResponse> getPetsByUser(Long userNo) {
        List<Pet> pets = petRepository.findByUserNo(userNo);
        return pets.stream()
                .map(this::toPetResponse)
                .collect(Collectors.toList());
    }

    // 반려동물 상세 조회
    public PetResponse getPetById(Long petNo) {
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));
        return toPetResponse(pet);
    }

    // 반려동물 수정
    @Transactional
    public PetResponse updatePet(Long petNo, Long userNo, PetRequest request) {
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        // 소유자 확인
        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물을 수정할 권한이 없습니다.");
        }

        // 이름 중복 체크 (자신 제외)
        if (!pet.getName().equals(request.getName()) && 
            petRepository.existsByUserNoAndName(userNo, request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 반려동물 이름입니다: " + request.getName());
        }

        pet.setName(request.getName());
        pet.setType(request.getType());
        pet.setAge(request.getAge());
        pet.setGender(request.getGender());
        pet.setWeight(request.getWeight());
        pet.setImageUrl(request.getImageUrl());
        pet.setSnsProfileNo(request.getSnsProfileNo());

        Pet updatedPet = petRepository.save(pet);
        return toPetResponse(updatedPet);
    }

    // 반려동물 삭제
    @Transactional
    public void deletePet(Long petNo, Long userNo) {
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        // 소유자 확인
        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물을 삭제할 권한이 없습니다.");
        }

        petRepository.delete(pet);
    }

    // PetStar 신청
    @Transactional
    public void applyPetStar(Long petNo, Long userNo) {
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        // 소유자 확인
        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물의 PetStar를 신청할 권한이 없습니다.");
        }

        // 이미 신청 중이거나 승인된 경우
        if (pet.getPetStarStatus() != PetStarStatus.NONE) {
            throw new IllegalArgumentException("이미 PetStar 신청이 진행 중이거나 처리되었습니다.");
        }

        pet.setPetStarStatus(PetStarStatus.PENDING);
        pet.setPendingAt(LocalDateTime.now());
        petRepository.save(pet);
    }

    // PetStar 목록 조회 (관리자용)
    public Page<PetStarResponse> getPetStarApplications(Pageable pageable) {
        Page<Pet> pets = petRepository.findByPetStarStatus(PetStarStatus.PENDING, pageable);
        
        // TODO: User 정보를 Feign Client로 가져와서 PetStarResponse 생성
        // 현재는 기본 정보만 반환
        return pets.map(pet -> new PetStarResponse(
                pet.getSnsProfileNo(),
                pet.getName(),
                "사용자명", // TODO: User 정보에서 가져오기
                "전화번호", // TODO: User 정보에서 가져오기
                "이메일"   // TODO: User 정보에서 가져오기
        ));
    }

    // PetStar 승인 (관리자용)
    @Transactional
    public void approvePetStar(Long petNo) {
        Pet pet = petRepository.findById(petNo)
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

    // Pet 엔티티를 PetResponse로 변환
    private PetResponse toPetResponse(Pet pet) {
        return PetResponse.builder()
                .petNo(pet.getPetNo())
                .userNo(pet.getUserNo())
                .name(pet.getName())
                .type(pet.getType())
                .imageUrl(pet.getImageUrl())
                .age(pet.getAge())
                .gender(pet.getGender())
                .weight(pet.getWeight())
                .isPetStar(pet.getIsPetStar())
                .snsProfileNo(pet.getSnsProfileNo())
                .petStarStatus(pet.getPetStarStatus())
                .pendingAt(pet.getPendingAt())
                .createdAt(pet.getCreatedAt())
                .updatedAt(pet.getUpdatedAt())
                .build();
    }
}
