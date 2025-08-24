package site.petful.healthservice.medical.dto;

import lombok.Data;
import java.util.List;

@Data
public class PrescriptionParsedDTO {
    private List<MedicationInfo> medications;
    private String templateName;
    private String inferResult;
    private String originalText;  // OCR로 추출된 원본 텍스트
    
    @Data
    public static class MedicationInfo {
        private String drugName;        // 약물명 (성분명)
        private String dosage;          // 용량
        private String administration;  // 복용법/용법
        private String frequency;       // 횟수 (하루 3번, 1회 등)
        private String prescriptionDays; // 처방일수
        private List<java.time.LocalTime> times; // 일정 시간들 (선택사항)
    }
}
