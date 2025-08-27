package site.petful.petservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.petservice.dto.PetRequest;
import site.petful.petservice.dto.PetResponse;
import site.petful.petservice.service.PetService;
import site.petful.petservice.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<List<PetResponse>>> getPets(@RequestAttribute("X-User-No") Long userNo) {
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
        petService.applyPetStar(petNo, userNo);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
