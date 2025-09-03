package site.petful.petservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.petservice.dto.PetRequest;
import site.petful.petservice.dto.PetResponse;
<<<<<<<< HEAD:pet-service/src/main/java/org/example/petservice/controller/PetController.java
========
import site.petful.petservice.dto.FileUploadResponse;
>>>>>>>> dev:pet-service/src/main/java/site/petful/petservice/controller/PetController.java
import site.petful.petservice.service.PetService;
import site.petful.petservice.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
//
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class PetController {

    private final PetService petService;

    // 반려동물 등록
    @PostMapping("/pets")
    public ResponseEntity<ApiResponse<PetResponse>> createPet(
            @RequestAttribute("X-User-No") Long userNo,
            @RequestBody PetRequest request) {
        PetResponse response = petService.createPet(userNo, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 반려동물 목록 조회
    @GetMapping("/pets")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPets(@RequestParam Long userNo) {
        List<PetResponse> pets = petService.getPetsByUser(userNo);
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    // 반려동물 목록 조회 (외부 사용자용)
    @GetMapping("/pets/external")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsExternal(@RequestParam Long userNo) {
        List<PetResponse> pets = petService.getPetsByUser(userNo);
        return ResponseEntity.ok(ApiResponse.success(pets));
    }

    // 반려동물 상세 조회
    @GetMapping("/pets/{petNo}")
    public ResponseEntity<ApiResponse<PetResponse>> getPet(@PathVariable Long petNo) {
        PetResponse pet = petService.getPetById(petNo);
        return ResponseEntity.ok(ApiResponse.success(pet));
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

<<<<<<<< HEAD:pet-service/src/main/java/org/example/petservice/controller/PetController.java
========
   
    // 펫스타 전체 조회
    @GetMapping("/petstars")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getAllPetStars() {
        List<PetResponse> petStars = petService.getAllPetStars();
        return ResponseEntity.ok(ApiResponse.success(petStars));
    }

    // petNos 리스트로 펫 조회
    @PostMapping("/petsByPetNos")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPetsByPetNos(@RequestBody List<Long> petNos) {
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

>>>>>>>> dev:pet-service/src/main/java/site/petful/petservice/controller/PetController.java
}
