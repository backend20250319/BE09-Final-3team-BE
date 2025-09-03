package site.petful.petservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.petful.petservice.dto.PortfolioImageFileResponse;
import site.petful.petservice.entity.History;
import site.petful.petservice.entity.PortfolioImageFile;
import site.petful.petservice.repository.HistoryRepository;
import site.petful.petservice.repository.PortfolioImageFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PortfolioImageFileService {

    private final PortfolioImageFileRepository portfolioImageFileRepository;
    private final HistoryRepository historyRepository;

    @Value("${ftp.view-url:http://dev.macacolabs.site:8008/3/pet/}")
    private String viewUrl;

    @Value("${ftp.base-folder:/3/pet/}")
    private String baseFolder;

    /**
     * 활동이력 이미지 업로드
     */
    public PortfolioImageFileResponse uploadHistoryImage(Long historyNo, MultipartFile file) throws IOException {
        // 활동이력 존재 확인
        History history = historyRepository.findById(historyNo)
                .orElseThrow(() -> new IllegalArgumentException("활동이력을 찾을 수 없습니다: " + historyNo));

        // 파일 저장
        String originalName = file.getOriginalFilename();
        String savedName = generateSavedFileName(historyNo, originalName);
        String filePath = baseFolder + savedName;

        // 실제 파일 저장 (FTP 업로드 로직은 별도 구현 필요)
        saveFileToFtp(file, filePath);

        // DB에 파일 정보 저장
        PortfolioImageFile portfolioImageFile = PortfolioImageFile.builder()
                .originalName(originalName)
                .savedName(savedName)
                .filePath(filePath)
                .historyNo(historyNo)
                .isDeleted(false)
                .build();

        PortfolioImageFile savedFile = portfolioImageFileRepository.save(portfolioImageFile);

        return PortfolioImageFileResponse.from(savedFile, viewUrl);
    }

    /**
     * 여러 활동이력 이미지 업로드
     */
    public List<PortfolioImageFileResponse> uploadHistoryImages(Long historyNo, List<MultipartFile> files) throws IOException {
        return files.stream()
                .map(file -> {
                    try {
                        return uploadHistoryImage(historyNo, file);
                    } catch (IOException e) {
                        throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 활동이력의 모든 이미지 조회
     */
    @Transactional(readOnly = true)
    public List<PortfolioImageFileResponse> getHistoryImages(Long historyNo) {
        List<PortfolioImageFile> files = portfolioImageFileRepository.findByHistoryNoAndIsDeletedFalse(historyNo);
        return files.stream()
                .map(file -> PortfolioImageFileResponse.from(file, viewUrl))
                .collect(Collectors.toList());
    }

    /**
     * 특정 반려동물의 모든 활동이력 이미지 조회
     */
    @Transactional(readOnly = true)
    public List<PortfolioImageFileResponse> getPetHistoryImages(Long petNo) {
        List<PortfolioImageFile> files = portfolioImageFileRepository.findByPetNo(petNo);
        return files.stream()
                .map(file -> PortfolioImageFileResponse.from(file, viewUrl))
                .collect(Collectors.toList());
    }

    /**
     * 활동이력 이미지 삭제 (논리적 삭제)
     */
    public void deleteHistoryImage(Long fileId) {
        PortfolioImageFile file = portfolioImageFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileId));

        file.setIsDeleted(true);
        portfolioImageFileRepository.save(file);
    }

    /**
     * 특정 활동이력의 모든 이미지 삭제 (활동이력 삭제 시)
     */
    public void deleteAllHistoryImages(Long historyNo) {
        List<PortfolioImageFile> files = portfolioImageFileRepository.findByHistoryNo(historyNo);
        files.forEach(file -> file.setIsDeleted(true));
        portfolioImageFileRepository.saveAll(files);
    }

    /**
     * 특정 반려동물의 모든 활동이력 이미지 삭제 (반려동물 삭제 시)
     */
    public void deleteAllPetHistoryImages(Long petNo) {
        List<PortfolioImageFile> files = portfolioImageFileRepository.findByPetNo(petNo);
        files.forEach(file -> file.setIsDeleted(true));
        portfolioImageFileRepository.saveAll(files);
    }

    /**
     * 저장된 파일명 생성
     */
    private String generateSavedFileName(Long historyNo, String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        
        // 밀리초까지 포함하여 고유성 보장
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        
        return "history_" + historyNo + "_" + timestamp + extension;
    }

    /**
     * FTP에 파일 저장 (실제 구현은 FTP 클라이언트 사용)
     */
    private void saveFileToFtp(MultipartFile file, String filePath) throws IOException {
        // TODO: 실제 FTP 업로드 로직 구현
        // 예시: Apache Commons Net의 FTPClient 사용
        log.info("FTP 업로드: {} -> {}", file.getOriginalFilename(), filePath);
        
        // 임시로 로컬에 저장 (개발 환경용)
        Path localPath = Paths.get("temp/" + filePath);
        Files.createDirectories(localPath.getParent());
        Files.copy(file.getInputStream(), localPath);
    }
}
