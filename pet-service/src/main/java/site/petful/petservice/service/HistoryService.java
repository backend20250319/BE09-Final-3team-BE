package site.petful.petservice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.petful.petservice.common.ftp.FtpService;
import site.petful.petservice.dto.HistoryRequest;
import site.petful.petservice.dto.HistoryResponse;
import site.petful.petservice.dto.MultipleFileUploadResponse;
import site.petful.petservice.dto.HistoryImageInfo;
import site.petful.petservice.entity.History;
import site.petful.petservice.entity.HistoryImageFile;
import site.petful.petservice.entity.Pet;
import site.petful.petservice.repository.HistoryRepository;
import site.petful.petservice.repository.HistoryImageFileRepository;
import site.petful.petservice.repository.PetRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final HistoryImageFileRepository historyImageFileRepository;
    private final PetRepository petRepository;
    private final FtpService ftpService;
    private final ObjectMapper objectMapper;

    // 활동이력 생성
    @Transactional
    public HistoryResponse createHistory(Long petNo, Long userNo, HistoryRequest request) {
        // 펫 존재 여부 및 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물의 활동이력을 생성할 권한이 없습니다.");
        }

        // 활동이력 생성
        History history = History.builder()
                .petNo(petNo)
                .historyStart(request.getHistoryStart())
                .historyEnd(request.getHistoryEnd())
                .title(request.getTitle() != null ? request.getTitle() : "활동 이력")
                .content(request.getContent())
                .build();

        History savedHistory = historyRepository.save(history);
        return toHistoryResponse(savedHistory);
    }

    // 활동이력 조회
    public HistoryResponse getHistory(Long petNo, Long historyNo, Long userNo) {
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        // 소유권 확인
        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력을 조회할 권한이 없습니다.");
        }

        return toHistoryResponse(history);
    }

    // 반려동물의 모든 활동이력 조회
    public List<HistoryResponse> getHistories(Long petNo, Long userNo) {
        // 펫 존재 여부 및 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 반려동물의 활동이력을 조회할 권한이 없습니다.");
        }

        List<History> histories = historyRepository.findByPetNo(petNo);
        return histories.stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    public List<HistoryResponse> getHistoriesExternal(Long petNo) {
        // 펫 존재 여부 및 소유권 확인
        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        List<History> histories = historyRepository.findByPetNo(petNo);
        return histories.stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // 활동이력 수정
    @Transactional
    public HistoryResponse updateHistory(Long petNo, Long historyNo, Long userNo, HistoryRequest request) {
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        // 소유권 확인
        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력을 수정할 권한이 없습니다.");
        }

        // 활동이력 정보 업데이트
        history.setHistoryStart(request.getHistoryStart());
        history.setHistoryEnd(request.getHistoryEnd());
        history.setTitle(request.getTitle() != null ? request.getTitle() : "활동 이력");
        history.setContent(request.getContent());

        History updatedHistory = historyRepository.save(history);
        return toHistoryResponse(updatedHistory);
    }

    // 활동이력 삭제
    @Transactional
    public void deleteHistory(Long petNo, Long historyNo, Long userNo) {
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        // 소유권 확인
        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력을 삭제할 권한이 없습니다.");
        }

        // 관련된 이미지 파일들 논리적 삭제
        historyImageFileRepository.softDeleteByHistoryNo(historyNo);
        
        // 활동이력 삭제
        historyRepository.delete(history);
    }

    // 활동이력 이미지 업로드
    @Transactional
    public MultipleFileUploadResponse uploadHistoryImages(List<MultipartFile> files, Long petNo, Long historyNo, Long userNo) {
        try {
            // 활동이력 존재 여부 및 소유권 확인
            History history = historyRepository.findById(historyNo)
                    .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

            if (!history.getPetNo().equals(petNo)) {
                throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
            }

            Pet pet = petRepository.findById(petNo)
                    .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

            if (!pet.getUserNo().equals(userNo)) {
                throw new IllegalArgumentException("해당 활동이력에 이미지를 업로드할 권한이 없습니다.");
            }

            // 파일 업로드
            List<String> uploadedUrls = ftpService.uploadMultiple(files);
          
            // 이미지 파일 정보를 데이터베이스에 저장
            List<HistoryImageFile> imageFiles = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String uploadedUrl = uploadedUrls.get(i);
                
                HistoryImageFile imageFile = HistoryImageFile.builder()
                        .historyNo(historyNo)
                        .originalName(file.getOriginalFilename())
                        .savedName(uploadedUrl.substring(uploadedUrl.lastIndexOf("/") + 1))
                        .filePath(uploadedUrl)
                        .isDeleted(false)
                        .build();
                
                imageFiles.add(imageFile);
            }
            
            historyImageFileRepository.saveAll(imageFiles);

            return MultipleFileUploadResponse.builder()
                    .success(true)
                    .message(uploadedUrls.size() + "개의 이미지가 성공적으로 업로드되었습니다.")
                    .fileUrls(uploadedUrls)
                    .uploadedCount(uploadedUrls.size())
                    .build();

        } catch (Exception e) {
            log.error("활동이력 이미지 업로드 실패: {}", e.getMessage());
            return MultipleFileUploadResponse.builder()
                    .success(false)
                    .message("이미지 업로드에 실패했습니다: " + e.getMessage())
                    .fileUrls(new ArrayList<>())
                    .uploadedCount(0)
                    .build();
        }
    }

    // 활동이력 이미지 삭제 (단일)
    @Transactional
    public void deleteHistoryImage(Long petNo, Long historyNo, Long imageId, Long userNo) {
        // 활동이력 존재 여부 및 소유권 확인
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력의 이미지를 삭제할 권한이 없습니다.");
        }

        // 이미지 파일 존재 여부 확인
        HistoryImageFile imageFile = historyImageFileRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("이미지 파일을 찾을 수 없습니다: " + imageId));

        if (!imageFile.getHistoryNo().equals(historyNo)) {
            throw new IllegalArgumentException("잘못된 활동이력 번호입니다.");
        }

        // 이미지 파일 논리적 삭제
        imageFile.setIsDeleted(true);
        historyImageFileRepository.save(imageFile);
    }

    // 활동이력 이미지 선택 삭제 (다중)
    @Transactional
    public void deleteHistoryImages(Long petNo, Long historyNo, List<Long> imageIds, Long userNo) {
        // 활동이력 존재 여부 및 소유권 확인
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력의 이미지를 삭제할 권한이 없습니다.");
        }

        // 이미지 파일들 존재 여부 및 소유권 확인
        List<HistoryImageFile> imageFiles = historyImageFileRepository.findAllById(imageIds);
        
        if (imageFiles.size() != imageIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 이미지 파일이 포함되어 있습니다.");
        }

        // 모든 이미지 파일이 해당 활동이력에 속하는지 확인
        for (HistoryImageFile imageFile : imageFiles) {
            if (!imageFile.getHistoryNo().equals(historyNo)) {
                throw new IllegalArgumentException("잘못된 활동이력 번호가 포함되어 있습니다.");
            }
        }

        // 이미지 파일들 논리적 삭제
        for (HistoryImageFile imageFile : imageFiles) {
            imageFile.setIsDeleted(true);
        }
        historyImageFileRepository.saveAll(imageFiles);
    }

    // 활동이력 이미지 정보 조회
    public List<HistoryImageInfo> getHistoryImages(Long petNo, Long historyNo, Long userNo) {
        // 활동이력 존재 여부 및 소유권 확인
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        if (!history.getPetNo().equals(petNo)) {
            throw new IllegalArgumentException("잘못된 반려동물 번호입니다.");
        }

        Pet pet = petRepository.findById(petNo)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다: " + petNo));

        if (!pet.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동이력의 이미지를 조회할 권한이 없습니다.");
        }

        // 이미지 파일 정보 조회
        List<HistoryImageFile> imageFiles = historyImageFileRepository
                .findByHistoryNoAndIsDeletedFalse(historyNo);

        return imageFiles.stream()
                .map(imageFile -> HistoryImageInfo.builder()
                        .id(imageFile.getId())
                        .url(imageFile.getFilePath())
                        .originalName(imageFile.getOriginalName())
                        .savedName(imageFile.getSavedName())
                        .historyNo(imageFile.getHistoryNo())
                        .build())
                .collect(Collectors.toList());
    }

    // DTO 변환 메서드
    private HistoryResponse toHistoryResponse(History history) {
        // 이미지 파일에서 상세 정보 가져오기
        List<HistoryImageInfo> images = historyImageFileRepository
                .findByHistoryNoAndIsDeletedFalse(history.getHistoryNo())
                .stream()
                .map(imageFile -> HistoryImageInfo.builder()
                        .id(imageFile.getId())
                        .url(imageFile.getFilePath())
                        .originalName(imageFile.getOriginalName())
                        .savedName(imageFile.getSavedName())
                        .historyNo(imageFile.getHistoryNo())
                        .build())
                .collect(Collectors.toList());

        // 기존 호환성을 위한 imageUrls도 유지
        List<String> imageUrls = images.stream()
                .map(HistoryImageInfo::getUrl)
                .collect(Collectors.toList());

        return HistoryResponse.builder()
                .historyNo(history.getHistoryNo())
                .historyStart(history.getHistoryStart())
                .historyEnd(history.getHistoryEnd())
                .title(history.getTitle())
                .content(history.getContent())
                .imageUrls(imageUrls)  // 기존 호환성
                .images(images)        // 새로운 상세 정보
                .petNo(history.getPetNo())
                .createdAt(history.getCreatedAt())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}