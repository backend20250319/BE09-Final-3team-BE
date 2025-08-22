package site.petful.healthservice.medical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;
import site.petful.healthservice.medical.ocr.ClovaOcrClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {
    
    private final ClovaOcrClient clovaOcrClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    

    
    public String processOcrImage(MultipartFile file) throws IOException {
        try {
        // 파일 검증
        validateFile(file);
        
        // 임시 파일 생성 및 OCR 처리
        File temp = null;
        try {
                String original = file.getOriginalFilename();
                if (original == null) {
                    original = "unknown";
                }
                String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            temp = File.createTempFile("ocr_", ext);
            file.transferTo(temp);
            
                // OCR 처리
                String ocrResult = clovaOcrClient.extractTextFromImage(temp);
                if (ocrResult == null || ocrResult.trim().isEmpty()) {
                    throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED, "OCR 결과가 비어있습니다.");
                }
                
                return ocrResult;
        } finally {
            if (temp != null && temp.exists()) temp.delete();
            }
        } catch (BusinessException e) {
            throw e; // BusinessException은 그대로 전파
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED, "OCR 처리 중 파일 오류가 발생했습니다: " + e.getMessage());
        } catch (RuntimeException e) {
            // ClovaOcrClient에서 발생한 설정 오류 등을 BusinessException으로 변환
            throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED, "OCR 설정 오류: " + e.getMessage());
        } catch (Exception e) {
            // 모든 예외를 BusinessException으로 변환하여 7000번대 에러코드 사용
            throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED, "OCR 처리 중 예상치 못한 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드된 파일이 없습니다.");
        }
        
        // 파일 크기 체크 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.IMAGE_SIZE_TOO_LARGE, "파일 크기가 10MB를 초과합니다.");
        }
        
        // 이미지 파일 형식 체크
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.IMAGE_FORMAT_NOT_SUPPORTED, "이미지 파일만 업로드 가능합니다.");
        }
    }
    
    /**
     * 처방전 이미지를 OCR로 분석하고 파싱된 정보를 반환합니다.
     */
    public PrescriptionParsedDTO processPrescription(MultipartFile file) throws IOException {
        // 파일 검증
        validateFile(file);
        
        // 임시 파일 생성 및 OCR 처리
        File temp = null;
        try {
            String original = file.getOriginalFilename();
            if (original == null) {
                original = "unknown";
            }
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            temp = File.createTempFile("prescription_", ext);
            file.transferTo(temp);
            
            // 1단계: Clova OCR로 텍스트 추출
            String ocrResponseJson = clovaOcrClient.extractTextFromImage(temp);
            
            // 2단계: OCR 응답 JSON 파싱
            return parsePrescription(ocrResponseJson);
        } finally {
            if (temp != null && temp.exists()) temp.delete();
        }
    }
    
    /**
     * OCR 응답 JSON을 파싱하여 처방전 정보를 추출합니다.
     */
    public PrescriptionParsedDTO parsePrescription(String ocrResponseJson) throws IOException {
        // JSON을 DTO로 변환 (간단한 구조로 파싱)
        // 실제로는 ClovaOcrResponseDTO를 사용해야 하지만, 여기서는 간단하게 처리
        return parsePrescriptionFromJson(ocrResponseJson);
    }
    
    /**
     * 간단한 JSON 파싱으로 처방전 정보를 추출합니다.
     */
    private PrescriptionParsedDTO parsePrescriptionFromJson(String json) {
        PrescriptionParsedDTO result = new PrescriptionParsedDTO();
        result.setOriginalText(json);
        result.setMedications(new ArrayList<>());
        
        try {
            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(json);
            
            // fields 배열에서 약물 정보 추출
            JsonNode fields = rootNode.get("fields");
            if (fields != null && fields.isArray()) {
                // 약물 정보를 임시로 저장할 맵
                List<PrescriptionParsedDTO.MedicationInfo> tempMedications = new ArrayList<>();
                
                for (JsonNode field : fields) {
                    String name = field.get("name").asText();
                    String value = field.get("inferText").asText();
                    
                    // 빈 값이면 건너뛰기
                    if (value == null || value.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 약물 번호 추출 (1번, 2번 등)
                    int medicationNumber = extractMedicationNumber(name);
                    if (medicationNumber > 0) {
                        // 해당 번호의 약물 정보가 없으면 생성
                        while (tempMedications.size() < medicationNumber) {
                            tempMedications.add(new PrescriptionParsedDTO.MedicationInfo());
                        }
                        
                        PrescriptionParsedDTO.MedicationInfo med = tempMedications.get(medicationNumber - 1);
                        
                        // 필드 타입에 따라 정보 설정
                        if (name.contains("성분명")) {
                            med.setDrugName(value);
                        } else if (name.contains("용량")) {
                            med.setDosage(value);
                        } else if (name.contains("용법")) {
                            // 줄바꿈 제거 및 텍스트 정리
                            String cleanedValue = value.replace("\n", " ").trim();
                            // "경구루", "경구두"를 "경구투여"로 통일
                            String normalizedValue = cleanedValue.replace("경구루", "경구투여")
                                                           .replace("경구두", "경구투여");
                            // "경구투여 여" → "경구투여"로 정리
                            normalizedValue = normalizedValue.replace("경구투여 여", "경구투여")
                                                           .replace("경구투여, 여", "경구투여")
                                                           .replace("경구투여 여,", "경구투여,");
                            // 연속된 공백을 하나로 정리
                            normalizedValue = normalizedValue.replaceAll("\\s+", " ").trim();
                            
                            med.setAdministration(normalizedValue);
                        } else if (name.contains("처방일수")) {
                            med.setPrescriptionDays(value);
                        }
                    }
                }
                
                // 유효한 약물 정보만 결과에 추가
                for (PrescriptionParsedDTO.MedicationInfo med : tempMedications) {
                    if (med.getDrugName() != null && !med.getDrugName().trim().isEmpty()) {
                        // 복용 빈도 추출 (용법에서 "하루 X회" 부분)
                        String frequency = extractFrequency(med.getAdministration());
                        med.setFrequency(frequency);
                        
                        result.getMedications().add(med);
                    }
                }
            }
            
            // 템플릿 이름 설정
            JsonNode matchedTemplate = rootNode.get("matchedTemplate");
            if (matchedTemplate != null && matchedTemplate.has("name")) {
                result.setTemplateName(matchedTemplate.get("name").asText());
            }
            
        } catch (Exception e) {
            // 파싱 실패 시 기본값 설정
            result.setMedications(new ArrayList<>());
            result.setTemplateName("처방전");
        }
        
        return result;
    }
    
    /**
     * 약물 번호를 추출합니다 (1번, 2번 등)
     */
    private int extractMedicationNumber(String fieldName) {
        if (fieldName.contains("1번")) return 1;
        if (fieldName.contains("2번")) return 2;
        if (fieldName.contains("3번")) return 3;
        if (fieldName.contains("4번")) return 4;
        if (fieldName.contains("5번")) return 5;
        return 0;
    }
    
    /**
     * 복용 빈도를 추출합니다 (용법에서 "하루 X회" 부분)
     */
    private String extractFrequency(String administration) {
        if (administration == null) return "";
        
        // "하루 X회" 패턴 찾기
        if (administration.contains("하루")) {
            int start = administration.indexOf("하루");
            int end = administration.indexOf("회");
            if (start >= 0 && end > start) {
                return administration.substring(start, end + 1);
            }
        }
        
        return "";
    }
}
