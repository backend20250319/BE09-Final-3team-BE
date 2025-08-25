package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertiser.AdvertiserRequest;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.security.SecurityUtil;
import site.petful.advertiserservice.service.AdvertiserService;

@RestController
@RequestMapping("/advertiser")
public class AdvertiserController {

    private final AdvertiserService advertiserService;
    private final SecurityUtil securityUtil;

    public AdvertiserController(AdvertiserService advertiserService, SecurityUtil securityUtil) {
        this.advertiserService = advertiserService;
        this.securityUtil = securityUtil;
    }

    // 1. 광고주 프로필 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getAdvertiser(@RequestParam Long advertiserNo) {
        try {
            // 본인의 프로필만 조회 가능하도록 권한 검증
            Long currentAdvertiserNo = securityUtil.getCurrentAdvertiserNo();
            if (!currentAdvertiserNo.equals(advertiserNo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, "본인의 프로필만 조회할 수 있습니다."));
            }
            
            AdvertiserResponse response = advertiserService.getAdvertiser(advertiserNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
        }
    }

    // 2. 광고주 프로필 정보 수정 (multipart/form-data)
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateAdvertiserWithFile(
            @RequestParam Long advertiserNo,
            @RequestPart("profile") AdvertiserRequest updateRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        try {
            // 본인의 프로필만 수정 가능하도록 권한 검증
            Long currentAdvertiserNo = securityUtil.getCurrentAdvertiserNo();
            if (!currentAdvertiserNo.equals(advertiserNo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, "본인의 프로필만 수정할 수 있습니다."));
            }
            
            AdvertiserResponse updatedResponse = advertiserService.updateAdvertiser(advertiserNo, updateRequest, imageFile);
            return ResponseEntity.ok(ApiResponseGenerator.success(updatedResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
        }
    }

    // 3. 광고주 프로필 정보 수정 (JSON)
    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<?>> updateAdvertiser(
            @RequestParam Long advertiserNo,
            @RequestBody AdvertiserRequest updateRequest) {
        try {
            // 본인의 프로필만 수정 가능하도록 권한 검증
            Long currentAdvertiserNo = securityUtil.getCurrentAdvertiserNo();
            if (!currentAdvertiserNo.equals(advertiserNo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, "본인의 프로필만 수정할 수 있습니다."));
            }
            
            AdvertiserResponse updatedResponse = advertiserService.updateAdvertiser(advertiserNo, updateRequest, null);
            return ResponseEntity.ok(ApiResponseGenerator.success(updatedResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.ADVERTISER_NOT_FOUND));
        }
    }
}