package site.petful.petservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import site.petful.petservice.dto.PetRequest;
import site.petful.petservice.dto.PetResponse;
import site.petful.petservice.dto.FileUploadResponse;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.entity.PetStarStatus;
import site.petful.petservice.repository.PetRepository;
import site.petful.petservice.common.ftp.FtpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final FtpService ftpService;

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
            String statusMessage = "";
            switch (pet.getPetStarStatus()) {
                case PENDING:
                    statusMessage = "이미 PetStar 신청이 진행 중입니다.";
                    break;
                case ACTIVE:
                    statusMessage = "이미 PetStar로 승인되어 활성화되었습니다.";
                    break;
                case REJECTED:
                    statusMessage = "이전에 PetStar 신청이 거절되었습니다.";
                    break;
                default:
                    statusMessage = "이미 PetStar 신청이 진행 중이거나 처리되었습니다.";
            }
            throw new IllegalArgumentException(statusMessage);
        }

        pet.setPetStarStatus(PetStarStatus.PENDING);
        pet.setPendingAt(LocalDateTime.now());
        petRepository.save(pet);
    }

    // PetStar 상태를 ACTIVE로 변경
    @Transactional
    public void activatePetStar(Long petNo) {
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        // 이미 ACTIVE 상태인 경우
        if (pet.getPetStarStatus() == PetStarStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 PetStar로 활성화되어 있습니다.");
        }

        // PetStar 상태를 ACTIVE로 변경하고 isPetStar를 true로 설정
        pet.setPetStarStatus(PetStarStatus.ACTIVE);
        pet.setIsPetStar(true);
        petRepository.save(pet);
    }

    // 펫스타 전체 조회
    public List<PetResponse> getAllPetStars() {
        List<Pet> petStars = petRepository.findByIsPetStarTrue();
        return petStars.stream()
                .map(this::toPetResponse)
                .collect(Collectors.toList());
    }

    // petNos 리스트로 펫 조회
    public List<PetResponse> getPetsByPetNos(List<Long> petNos) {
        List<Pet> pets = petRepository.findByPetNos(petNos);
        return pets.stream()
                .map(this::toPetResponse)
                .collect(Collectors.toList());
    }

    // 반려동물 이미지 업로드
    @Transactional
    public FileUploadResponse uploadPetImage(MultipartFile file, Long petNo, Long userNo) {
        // 반려동물 존재 여부 및 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물의 이미지를 업로드할 권한이 없습니다.");
        }

        // 파일 업로드
        String filename = ftpService.upload(file);
        
        if (filename == null) {
            return FileUploadResponse.builder()
                    .success(false)
                    .message("파일 업로드에 실패했습니다.")
                    .build();
        }

        // 반려동물 이미지 URL 업데이트
        String fileUrl = ftpService.getFileUrl(filename);
        pet.setImageUrl(fileUrl);
        petRepository.save(pet);

        return FileUploadResponse.builder()
                .success(true)
                .message("반려동물 이미지가 성공적으로 업로드되었습니다.")
                .filename(filename)
                .fileUrl(fileUrl)
                .build();
    }

    // Pet 엔티티를 PetResponse로 변환
    private PetResponse toPetResponse(Pet pet) {
        log.debug("Pet 엔티티 변환 - petNo: {}, imageUrl: {}, snsUrl: {}", 
                  pet.getPetNo(), pet.getImageUrl(), pet.getSnsUrl());
        
        PetResponse response = PetResponse.builder()
                .petNo(pet.getPetNo())
                .userNo(pet.getUserNo())
                .name(pet.getName())
                .type(pet.getType())
                .imageUrl(pet.getImageUrl())
                .snsUrl(pet.getSnsUrl())  // snsUrl 추가
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
        
        log.debug("PetResponse 생성 완료 - snsUrl: {}", response.getSnsUrl());
        return response;
    }
}
