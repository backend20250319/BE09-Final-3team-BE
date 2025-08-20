package site.petful.healthservice.medical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.medical.common.ApiResponse;
import site.petful.healthservice.medical.common.ApiResponseGenerator;
import site.petful.healthservice.medical.common.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicationController {

    @Value("${storage.medication-dir:uploads/medication}")
    private String medicationDir;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadMedication(@RequestParam("file") MultipartFile file) {
        try {
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
            return ResponseEntity.ok(ApiResponseGenerator.success(publicPath));

        } catch (IOException e) {
            // 실패 응답
            return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.OPERATION_FAILED, "파일 저장 실패", null));
        }
    }
}
