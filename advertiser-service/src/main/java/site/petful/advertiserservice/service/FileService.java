package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.common.ftp.FtpService;
import site.petful.advertiserservice.dto.advertisement.ImageUploadResponse;
import site.petful.advertiserservice.dto.advertiser.FileMetaUpdateRequest;
import site.petful.advertiserservice.dto.advertiser.FileUploadResponse;
import site.petful.advertiserservice.dto.advertiser.AdvertiserWithFilesResponse;
import site.petful.advertiserservice.entity.advertisement.AdFiles;
import site.petful.advertiserservice.entity.advertisement.Advertisement;
import site.petful.advertiserservice.entity.advertiser.Advertiser;
import site.petful.advertiserservice.entity.advertiser.AdvertiserFiles;
import site.petful.advertiserservice.entity.advertiser.FileType;
import site.petful.advertiserservice.repository.AdRepository;
import site.petful.advertiserservice.repository.AdvertiserRepository;
import site.petful.advertiserservice.repository.FileRepository;
import site.petful.advertiserservice.repository.ImageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private final AdRepository adRepository;
    private final AdvertiserRepository advertiserRepository;
    private final FtpService ftpService;
    private final ImageRepository imageRepository;
    private final FileRepository fileRepository;

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

    // 1-2. 광고주 파일 업로드
    public List<FileUploadResponse> uploadFile(MultipartFile file, MultipartFile image, Long advertiserNo) {
        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        // 파일 검증
        if (file == null || file.isEmpty()) {
            throw new RuntimeException(ErrorCode.FILE_EMPTY.getDefaultMessage());
        }

        // 파일 크기 검증 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException(ErrorCode.FILE_SIZE_EXCEEDED.getDefaultMessage());
        }

        if (image != null) {
            if (image.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException(ErrorCode.FILE_SIZE_EXCEEDED.getDefaultMessage());
            }

            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException(ErrorCode.FILE_TYPE_IMAGE.getDefaultMessage());
            }
        }

        List<FileUploadResponse> response = new ArrayList<>();
        response.add(uploadAndSaveFile(file, FileType.DOC, advertiser));

        if (image != null && !image.isEmpty()) {
            response.add(uploadAndSaveFile(image, FileType.PROFILE, advertiser));
        }

        return response;
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

    // 2-2. 광고주 파일 조회
    @Transactional(readOnly = true)
    public List<FileUploadResponse> getFileByAdvertiserNo(Long advertiserNo) {

        advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        List<AdvertiserFiles> files = fileRepository.findByAdvertiser_AdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.FILE_NOT_FOUND.getDefaultMessage()));

        return files.stream()
                .map(FileUploadResponse::from)
                .collect(Collectors.toList());
    }

    // 광고주 정보와 파일을 함께 조회하는 메서드
    @Transactional(readOnly = true)
    public AdvertiserWithFilesResponse getAdvertiserWithFiles(Long advertiserNo) {
        log.info("🔍 [FileService] 광고주 정보 및 파일 조회 시작: advertiserNo={}", advertiserNo);
        
        // 광고주 정보 조회
        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> {
                    log.error("❌ [FileService] 광고주를 찾을 수 없음: advertiserNo={}", advertiserNo);
                    return new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage());
                });
        log.info("✅ [FileService] 광고주 존재 확인: advertiserNo={}, name={}", advertiserNo, advertiser.getName());

        // 파일 조회
        List<AdvertiserFiles> files = fileRepository.findByAdvertiser_AdvertiserNo(advertiserNo)
                .orElse(new ArrayList<>());
        log.info("📁 [FileService] 조회된 파일 수: advertiserNo={}, fileCount={}", advertiserNo, files.size());
        
        for (AdvertiserFiles file : files) {
            log.info("📄 [FileService] 파일 정보: fileNo={}, type={}, originalName={}, filePath={}", 
                    file.getFileNo(), file.getType(), file.getOriginalName(), file.getFilePath());
        }

        AdvertiserWithFilesResponse response = AdvertiserWithFilesResponse.from(advertiser, files);
        log.info("✅ [FileService] 광고주 정보 및 파일 조회 완료: advertiserNo={}, profileUrl={}, documentUrl={}", 
                advertiserNo, response.getProfileImageUrl(), response.getDocumentUrl());
        
        return response;
    }

    // 3-1. 광고 이미지 수정 (이미지 삭제 및 새 이미지 업로드로 처리)
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

    // 3-2. 광고주 파일 수정 (파일 삭제 및 새 파일 업로드로 처리)
    public List<FileUploadResponse> updateFile(Long advertiserNo,
                                               MultipartFile newFile,
                                               MultipartFile newImage,
                                               FileMetaUpdateRequest request) {

        Advertiser advertiser = advertiserRepository.findByAdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADVERTISER_NOT_FOUND.getDefaultMessage()));

        List<AdvertiserFiles> files = fileRepository.findByAdvertiser_AdvertiserNo(advertiserNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.FILE_NOT_FOUND.getDefaultMessage()));

        List<FileUploadResponse> responses = new ArrayList<>();

        FileUploadResponse fileResponse = updateSingleFile(files, FileType.DOC, newFile, request != null ? request.getIsDeletedForFile() : null, advertiser);
        if (fileResponse != null) {
            responses.add(fileResponse);
        }

        FileUploadResponse imageResponse = updateSingleFile(files, FileType.PROFILE, newImage, request != null ? request.getIsDeletedForImage() : null, advertiser);
        if (imageResponse != null) {
            responses.add(imageResponse);
        }

        responses.addAll(files.stream()
                .filter(f -> (newFile == null || f.getType() != FileType.DOC) && (newImage == null || f.getType() != FileType.PROFILE))
                .map(FileUploadResponse::from)
                .toList());

        return responses;
    }

    private FileUploadResponse uploadAndSaveFile(MultipartFile multipartFile, FileType type, Advertiser advertiser) {
        String uploadedFileName = ftpService.upload("advertiser/", multipartFile);
        String fileUrl = ftpService.getFileUrl("advertiser/", uploadedFileName);

        AdvertiserFiles files = new AdvertiserFiles();
        files.setOriginalName(multipartFile.getOriginalFilename());
        files.setSavedName(uploadedFileName);
        files.setFilePath(fileUrl);
        files.setCreatedAt(LocalDateTime.now());
        files.setIsDeleted(false);
        files.setType(type);
        files.setAdvertiser(advertiser);

        AdvertiserFiles saved = fileRepository.save(files);
        return FileUploadResponse.from(saved);
    }

    private void updateIsDeletedStatus(Long fileNo, Boolean isDeleted) {
        if (fileNo == null) {
            return;
        }
        AdvertiserFiles file = fileRepository.findById(fileNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.FILE_NOT_FOUND.getDefaultMessage()));
        file.setIsDeleted(isDeleted);
        fileRepository.save(file);
    }

    private FileUploadResponse updateSingleFile(List<AdvertiserFiles> files,
                                                FileType type,
                                                MultipartFile newFile,
                                                Boolean isDeleted,
                                                Advertiser advertiser) {

        AdvertiserFiles existing = files.stream()
                .filter(f -> f.getType() == type)
                .findFirst()
                .orElse(null);

        if (newFile != null && !newFile.isEmpty()) {
            if (newFile.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException(ErrorCode.FILE_SIZE_EXCEEDED.getDefaultMessage());
            }
            if (existing == null) {
                return uploadAndSaveFile(newFile, type, advertiser);
            } else {
                String uploadedName = ftpService.upload("advertiser/", newFile);
                String fileUrl = ftpService.getFileUrl("advertiser/", uploadedName);

                existing.setOriginalName(newFile.getOriginalFilename());
                existing.setSavedName(uploadedName);
                existing.setFilePath(fileUrl);
                fileRepository.save(existing);

                return FileUploadResponse.from(existing);
            }
        } else if (isDeleted != null) {
            if (existing == null) {
                throw new RuntimeException(ErrorCode.FILE_NOT_FOUND.getDefaultMessage());
            }
            updateIsDeletedStatus(existing.getFileNo(), isDeleted);
            return FileUploadResponse.from(existing);
        }
        return null;
    }

}
