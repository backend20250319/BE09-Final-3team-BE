package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.AdvertiserRequest;
import site.petful.advertiserservice.dto.AdvertiserResponse;
import site.petful.advertiserservice.service.AdvertiserService;

@RestController
@RequestMapping("/advertiser")
public class AdvertiserController {

    private final AdvertiserService advertiserService;

    public AdvertiserController(AdvertiserService advertiserService) {
        this.advertiserService = advertiserService;
    }

    // 1. 광고주 프로필 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getAdvertiser(@RequestParam Long advertiserNo) {
        try {
            AdvertiserResponse response = advertiserService.getAdvertiser(advertiserNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }

    // 2. 광고주 프로필 정보 수정
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateAdvertiser(
            @RequestParam Long advertiserNo,
            @RequestPart("profile") AdvertiserRequest updateRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            AdvertiserResponse updatedResponse = advertiserService.updateAdvertiser(advertiserNo, updateRequest, imageFile);
            return ResponseEntity.ok(ApiResponseGenerator.success(updatedResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.NOT_FOUND, e.getMessage()));
        }
    }
}
