package site.petful.healthservice.medical.medication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.medical.medication.dto.PrescriptionParsedDTO;
import site.petful.healthservice.medical.ocr.ClovaOcrClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {
    
    private final ClovaOcrClient clovaOcrClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public PrescriptionParsedDTO parsePrescription(String ocrResponseJson) {
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
            
            // images 배열에서 첫 번째 이미지의 fields 추출
            JsonNode images = rootNode.get("images");
            System.out.println("=== DEBUG: Parsing started ===");
            System.out.println("Images array size: " + (images != null ? images.size() : "null"));
            
            if (images != null && images.isArray() && images.size() > 0) {
                JsonNode firstImage = images.get(0);
                JsonNode fields = firstImage.get("fields");
                System.out.println("Fields array size: " + (fields != null ? fields.size() : "null"));
                
                if (fields != null && fields.isArray()) {
                    // 약물 정보를 임시로 저장할 맵
                    List<PrescriptionParsedDTO.MedicationInfo> tempMedications = new ArrayList<>();
                    
                    for (JsonNode field : fields) {
                        String name = field.get("name").asText();
                        String value = field.get("inferText").asText();
                        
                        System.out.println("Processing field: " + name + " = " + value);
                        
                        // 빈 값이면 건너뛰기
                        if (value == null || value.trim().isEmpty()) {
                            System.out.println("Skipping empty field: " + name);
                            continue;
                        }
                        
                        // 약물 번호 추출 (1번, 2번 등)
                        int medicationNumber = extractMedicationNumber(name);
                        System.out.println("Medication number: " + medicationNumber + " from: " + name);
                        
                        if (medicationNumber > 0) {
                            // 해당 번호의 약물 정보가 없으면 생성
                            while (tempMedications.size() < medicationNumber) {
                                tempMedications.add(new PrescriptionParsedDTO.MedicationInfo());
                            }
                            
                            PrescriptionParsedDTO.MedicationInfo med = tempMedications.get(medicationNumber - 1);
                            
                            // 필드 타입에 따라 정보 설정
                            if (name.contains("성분명") || name.contains("Field 01") || name.contains("Field 02")) {
                                med.setDrugName(value);
                            } else if (name.contains("용량") || name.contains("Field 06") || name.contains("Field 07")) {
                                med.setDosage(value);
                            } else if (name.contains("용법") || name.contains("Field 11") || name.contains("Field 12")) {
                                // 줄바꿈 제거 및 텍스트 정리
                                String cleanedValue = value.replace("\n", " ").trim();
                                // "경구투"를 "경구투여"로 통일
                                String normalizedValue = cleanedValue.replace("경구투", "경구투여");
                                // "경구투여 여" → "경구투여"로 정리
                                normalizedValue = normalizedValue.replace("경구투여 여", "경구투여")
                                                               .replace("경구투여, 여", "경구투여")
                                                               .replace("경구투여 여,", "경구투여,");
                                // "경구투여 여" → "경구투여"로 정리 (공백 제거)
                                normalizedValue = normalizedValue.replace("경구투여 여", "경구투여");
                                // 연속된 공백을 하나로 정리
                                normalizedValue = normalizedValue.replaceAll("\\s+", " ").trim();
                                
                                med.setAdministration(normalizedValue);
                            } else if (name.contains("처방일수") || name.contains("Field 16") || name.contains("Field 17")) {
                                med.setPrescriptionDays(value);
                            }
                        }
                    }
                    
                    // 유효한 약물 정보만 결과에 추가
                    System.out.println("Temp medications size: " + tempMedications.size());
                    for (PrescriptionParsedDTO.MedicationInfo med : tempMedications) {
                        System.out.println("Checking medication: drugName=" + med.getDrugName() + ", dosage=" + med.getDosage());
                        if (med.getDrugName() != null && !med.getDrugName().trim().isEmpty()) {
                            // 복용 빈도 추출 (용법에서 "하루 X회" 부분)
                            String frequency = extractFrequency(med.getAdministration());
                            med.setFrequency(frequency);
                            
                            result.getMedications().add(med);
                            System.out.println("Added medication: " + med.getDrugName());
                        }
                    }
                    System.out.println("Final result medications size: " + result.getMedications().size());
                }
                
                // 템플릿 이름 설정 (images[0]에서 가져오기)
                JsonNode matchedTemplate = firstImage.get("matchedTemplate");
                if (matchedTemplate != null && matchedTemplate.has("name")) {
                    result.setTemplateName(matchedTemplate.get("name").asText());
                }
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
        // "Field 01", "Field 02" 형식 지원
        if (fieldName.contains("Field 01") || fieldName.contains("1번")) return 1;
        if (fieldName.contains("Field 02") || fieldName.contains("2번")) return 2;
        if (fieldName.contains("Field 06") || fieldName.contains("1번 용량")) return 1;
        if (fieldName.contains("Field 07") || fieldName.contains("2번 용량")) return 2;
        if (fieldName.contains("Field 11") || fieldName.contains("1번 용법")) return 1;
        if (fieldName.contains("Field 12") || fieldName.contains("2번 용법")) return 2;
        if (fieldName.contains("Field 16") || fieldName.contains("1번 처방일수")) return 1;
        if (fieldName.contains("Field 17") || fieldName.contains("2번 처방일수")) return 2;
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


    
    /**
     * 파일 검증
     */
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
}
