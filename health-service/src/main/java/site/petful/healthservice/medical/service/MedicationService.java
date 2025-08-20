package site.petful.healthservice.medical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.medical.exception.InvalidFileException;
import site.petful.healthservice.medical.ocr.ClovaOcrClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicationService {
    
    @Value("${storage.medication-dir:uploads/medication}")
    private String medicationDir;
    
    private final ClovaOcrClient clovaOcrClient;
    
    public String uploadMedicationImage(MultipartFile file) throws IOException {
        // 파일 검증
        validateFile(file);
        
        // 1) 저장 디렉토리 생성 (애플리케이션 루트 기준)
        String configuredDir = medicationDir == null ? "uploads/medication" : medicationDir.replace("\\", "/");
        if (configuredDir.startsWith("/")) configuredDir = configuredDir.substring(1);
        String baseDir = System.getProperty("user.dir");
        Path dirPath = Paths.get(baseDir, configuredDir);
        Files.createDirectories(dirPath);

        // 2) 파일명 생성 (경로 traversal 방지 + UUID로 충돌 방지)
        String original = Objects.requireNonNull(file.getOriginalFilename(), "filename");
        String safeOriginal = new File(original).getName();
        String ext = safeOriginal.contains(".") ? safeOriginal.substring(safeOriginal.lastIndexOf('.')) : "";
        String savedName = UUID.randomUUID() + ext;

        // 3) 저장
        Path target = dirPath.resolve(savedName);
        file.transferTo(target.toFile());

        // 4) 클라이언트에 반환할 상대 경로 (슬래시 기준)
        String publicPath = "/" + configuredDir + "/" + savedName;
        publicPath = publicPath.replace("//", "/");
        return publicPath;
    }
    
    public String processOcrImage(MultipartFile file) throws IOException {
        // 파일 검증
        validateFile(file);
        
        // 임시 파일 생성 및 OCR 처리
        File temp = null;
        try {
            String original = Objects.requireNonNull(file.getOriginalFilename(), "filename");
            String safeOriginal = new File(original).getName();
            String ext = safeOriginal.contains(".") ? safeOriginal.substring(safeOriginal.lastIndexOf('.')) : "";
            temp = File.createTempFile("ocr_", ext);
            file.transferTo(temp);
            
            return clovaOcrClient.extractTextFromImage(temp);
        } finally {
            if (temp != null && temp.exists()) temp.delete();
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("업로드된 파일이 없습니다.");
        }
        
        // 파일 크기 체크 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new InvalidFileException("파일 크기가 10MB를 초과합니다.");
        }
        
        // 이미지 파일 형식 체크
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileException("이미지 파일만 업로드 가능합니다.");
        }
    }
}
