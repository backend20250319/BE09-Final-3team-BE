package site.petful.petservice.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.petful.petservice.client.InstagramProfileClient;
import site.petful.petservice.common.ApiResponse;
import site.petful.petservice.dto.FileUploadResponse;
import site.petful.petservice.dto.InstagramProfileInfo;
import site.petful.petservice.dto.PetRequest;
import site.petful.petservice.dto.PetResponse;
import site.petful.petservice.service.PetService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class PetController {

    private final PetService petService;
    private final InstagramProfileClient instagramProfileClient;

    // 반려동물 등록
    @PostMapping("/pets")
    public ResponseEntity<ApiResponse<PetResponse>> createPet(
        @RequestAttribute("X-User-No") Long userNo,
        @RequestBody PetRequest request) {
        PetResponse response = petService.createPet(userNo, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 반려동물 수정
    @PutMapping("/pets/{petNo}")
    public ResponseEntity<ApiResponse<PetResponse>> updatePet(
        @PathVariable Long petNo,
        @RequestAttribute("X-User-No") Long userNo,
        @RequestBody PetRequest request) {
        PetResponse response = petService.updatePet(petNo, userNo, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 반려동물 목록 조회
    @GetMapping("/pets")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPets(
        @RequestAttribute("X-User-No") Long userNo) {
        List<PetResponse> pets = petService.getPetsByUser(userNo);
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    // 반려동물 목록 조회 (외부 사용자용)
    @GetMapping("/pets/external")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsExternal(
        @RequestParam Long userNo) {
        List<PetResponse> pets = petService.getPetsByUser(userNo);
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    // 반려동물 상세 조회
    @GetMapping("/pets/{petNo}")
    public ResponseEntity<ApiResponse<PetResponse>> getPet(@PathVariable Long petNo) {
        PetResponse pet = petService.getPetById(petNo);
        return ResponseEntity.ok(ApiResponse.success(pet));
    }


    // 반려동물 삭제
    @DeleteMapping("/pets/{petNo}")
    public ResponseEntity<ApiResponse<Void>> deletePet(
        @PathVariable Long petNo,
        @RequestAttribute("X-User-No") Long userNo) {
        petService.deletePet(petNo, userNo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // PetStar 신청
    @PostMapping("/pets/{petNo}/petstar/apply")
    public ResponseEntity<ApiResponse<Void>> applyPetStar(
        @PathVariable Long petNo,
        @RequestAttribute("X-User-No") Long userNo) {
        try {
            petService.applyPetStar(petNo, userNo);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (IllegalArgumentException e) {
            log.error("PetStar 신청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 펫스타 전체 조회
    @GetMapping("/petstars")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getAllPetStars() {
        List<PetResponse> petStars = petService.getAllPetStars();
        return ResponseEntity.ok(ApiResponse.success(petStars));
    }

    // petNos 리스트로 펫 조회
    @PostMapping("/petsByPetNos")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsByPetNos(
        @RequestBody List<Long> petNos) {
        List<PetResponse> pets = petService.getPetsByPetNos(petNos);
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    // 반려동물 이미지 업로드
    @PostMapping("/pets/{petNo}/image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadPetImage(
        @PathVariable Long petNo,
        @RequestAttribute("X-User-No") Long userNo,
        @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = petService.uploadPetImage(file, petNo, userNo);

        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }

    // Instagram 프로필 연결
    @PostMapping("/pets/{petNo}/instagram/connect")
    public ResponseEntity<ApiResponse<PetResponse>> connectInstagramProfile(
        @PathVariable Long petNo,
        @RequestAttribute("X-User-No") Long userNo,
        @RequestParam Long instagramProfileId) {
        try {
            PetResponse response = petService.connectInstagramProfile(petNo, userNo,
                instagramProfileId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("Instagram 프로필 연결 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Instagram 프로필 연결 해제
    @DeleteMapping("/pets/{petNo}/instagram/disconnect")
    public ResponseEntity<ApiResponse<PetResponse>> disconnectInstagramProfile(
        @PathVariable Long petNo,
        @RequestAttribute("X-User-No") Long userNo) {
        try {
            PetResponse response = petService.disconnectInstagramProfile(petNo, userNo);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.error("Instagram 프로필 연결 해제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 사용자의 Instagram 프로필 목록 조회 (연결 가능한 프로필들)
    @GetMapping("/instagram/profiles")
    public ResponseEntity<ApiResponse<List<InstagramProfileInfo>>> getAvailableInstagramProfiles(
        @RequestAttribute("X-User-No") Long userNo) {
        try {
            List<InstagramProfileInfo> profiles = instagramProfileClient.getProfilesByUser(userNo);
            return ResponseEntity.ok(ApiResponse.success(profiles));
        } catch (Exception e) {
            log.error("Instagram 프로필 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Instagram 프로필 목록을 가져올 수 없습니다."));
        }
    }

}
