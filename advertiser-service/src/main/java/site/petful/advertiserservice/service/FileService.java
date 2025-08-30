package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.common.ftp.FtpService;
import site.petful.advertiserservice.dto.advertisement.ImageUploadResponse;
import site.petful.advertiserservice.entity.advertisement.AdFiles;
import site.petful.advertiserservice.entity.advertisement.Advertisement;
import site.petful.advertiserservice.repository.AdRepository;
import site.petful.advertiserservice.repository.ImageRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final AdRepository adRepository;
    private final FtpService ftpService;
    private final ImageRepository imageRepository;

    // 1-1. 광고 이미지 업로드
    public ImageUploadResponse uploadImage(MultipartFile file, Long adNo) {
        Advertisement ad = adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        // 파일 검증
        if (file.isEmpty()) {
            throw new RuntimeException(ErrorCode.FILE_EMPTY.getDefaultMessage());
        }

        // 파일 크기 검증 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException(ErrorCode.FILE_SIZE_EXCEEDED.getDefaultMessage());
        }

        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException(ErrorCode.FILE_TYPE_IMAGE.getDefaultMessage());
        }

        String uploadedFilename = ftpService.upload("advertisement/", file);
        String fileUrl = ftpService.getFileUrl("advertisement/", uploadedFilename);

        AdFiles adFiles = new AdFiles();
        adFiles.setOriginalName(file.getOriginalFilename());
        adFiles.setSavedName(uploadedFilename);
        adFiles.setFilePath(fileUrl);
        adFiles.setCreatedAt(LocalDateTime.now());
        adFiles.setIsDeleted(false);
        adFiles.setAdvertisement(ad);

        AdFiles saved = imageRepository.save(adFiles);

        return ImageUploadResponse.from(saved);

    }

    // 2-1. 광고 이미지 조회
    @Transactional(readOnly = true)
    public ImageUploadResponse getImageByAdNo(Long adNo) {

        adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        AdFiles file = imageRepository.findByAdvertisement_AdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.FILE_NOT_FOUND.getDefaultMessage()));

        return ImageUploadResponse.from(file);
    }

    // 3-1. 광고 이미지 수정 (파일 삭제 및 새 파일 업로드로 처리)
    public ImageUploadResponse updateImage(Long adNo, MultipartFile newFile, Boolean isDeleted) {

        adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        AdFiles file = imageRepository.findByAdvertisement_AdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.FILE_NOT_FOUND.getDefaultMessage()));

        // 파일 변경이 있을 경우
        if (newFile != null && !newFile.isEmpty()) {
            if (newFile.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException(ErrorCode.FILE_SIZE_EXCEEDED.getDefaultMessage());
            }

            // 새 파일 업로드
            String uploadedFilename = ftpService.upload("advertisement/", newFile);
            String fileUrl = ftpService.getFileUrl("advertisement/", uploadedFilename);

            file.setOriginalName(newFile.getOriginalFilename());
            file.setSavedName(uploadedFilename);
            file.setFilePath(fileUrl);
        }

        // isDeleted 값이 전달되면 수정
        if (isDeleted != null) {
            file.setIsDeleted(isDeleted);
        }

        AdFiles updatedFile = imageRepository.save(file);

        return ImageUploadResponse.from(updatedFile);
    }
}
