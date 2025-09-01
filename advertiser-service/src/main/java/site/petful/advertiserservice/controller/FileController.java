package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.advertisement.ImageUploadResponse;
import site.petful.advertiserservice.dto.advertiser.FileMetaUpdateRequest;
import site.petful.advertiserservice.dto.advertiser.FileUploadResponse;
import site.petful.advertiserservice.service.FileService;

import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // 1-1. 광고 이미지 업로드
    @PostMapping("/ad/{adNo}")
    public ResponseEntity<ApiResponse<?>> uploadImage(
            @RequestPart("image") MultipartFile file,
            @PathVariable Long adNo) {

        try {
            ImageUploadResponse response = fileService.uploadImage(file, adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

    // 1-2. 광고주 파일 업로드
    @PostMapping("/advertiser/{advertiserNo}")
    public ResponseEntity<ApiResponse<?>> uploadFile(
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @PathVariable Long advertiserNo) {

        try {
            List<FileUploadResponse> response = fileService.uploadFile(file, image, advertiserNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

    // 2-1. 광고 이미지 조회
    @GetMapping("/ad/{adNo}")
    public ResponseEntity<ApiResponse<?>> getImageByAdNo(@PathVariable Long adNo) {
        try {
            ImageUploadResponse response = fileService.getImageByAdNo(adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 2-2. 광고주 파일 조회
    @GetMapping("/advertiser/{advertiserNo}")
    public ResponseEntity<ApiResponse<?>> getFileByAdvertiserNo(@PathVariable Long advertiserNo) {
        try {
            List<FileUploadResponse> response = fileService.getFileByAdvertiserNo(advertiserNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 3-1. 광고 이미지 수정 (이미지 삭제 및 새 이미지 업로드로 처리)
    @PutMapping("/ad/{adNo}")
    public ResponseEntity<ApiResponse<?>> updateImage(
            @PathVariable Long adNo,
            @RequestPart(value = "file", required = false) MultipartFile newFile,
            @RequestParam(required = false) Boolean isDeleted) {

        try {
            ImageUploadResponse response = fileService.updateImage(adNo, newFile, isDeleted);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

    // 3-2. 광고주 파일 수정 (파일 삭제 및 새 파일 업로드로 처리)
    @PutMapping("/advertiser/{advertiserNo}")
    public ResponseEntity<ApiResponse<?>> updateFile(
            @PathVariable Long advertiserNo,
            @RequestPart(value = "file", required = false) MultipartFile newFile,
            @RequestPart(value = "image", required = false) MultipartFile newImage,
            @RequestPart(value = "fileMeta", required = false) FileMetaUpdateRequest request) {

        try {
            List<FileUploadResponse> response = fileService.updateFile(advertiserNo, newFile, newImage, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
        }
    }

}
