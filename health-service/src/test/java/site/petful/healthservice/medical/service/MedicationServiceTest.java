package site.petful.healthservice.medical.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;
import site.petful.healthservice.medical.ocr.ClovaOcrClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private ClovaOcrClient clovaOcrClient;

    @InjectMocks
    private MedicationService medicationService;

    private MockMultipartFile mockImageFile;
    private String sampleOcrResponseJson;

    @BeforeEach
    void setUp() {
        // 테스트용 이미지 파일 생성
        mockImageFile = new MockMultipartFile(
            "file", 
            "prescription.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        // 테스트용 OCR 응답 JSON
        sampleOcrResponseJson = """
            {
                "uid": "test-uid",
                "inferResult": "SUCCESS",
                "fields": [
                    {
                        "name": "1번 성분명",
                        "inferText": "Amoxicillin (항생제)",
                        "inferConfidence": 0.9823
                    },
                    {
                        "name": "1번 용량",
                        "inferText": "50mg",
                        "inferConfidence": 0.9996
                    },
                    {
                        "name": "1번 용법",
                        "inferText": "경구투여 여, 하루 2회",
                        "inferConfidence": 0.9957
                    },
                    {
                        "name": "1번 처방일수",
                        "inferText": "3일",
                        "inferConfidence": 0.9993
                    },
                    {
                        "name": "2번 성분명",
                        "inferText": "Firocoxib (소염진통제)",
                        "inferConfidence": 0.9979
                    },
                    {
                        "name": "2번 용량",
                        "inferText": "57mg",
                        "inferConfidence": 0.9996
                    },
                    {
                        "name": "2번 용법",
                        "inferText": "경구투여 여, 하루 1회",
                        "inferConfidence": 0.9855
                    },
                    {
                        "name": "2번 처방일수",
                        "inferText": "7일",
                        "inferConfidence": 1.0
                    }
                ]
            }
            """;
    }

    @Test
    void processPrescription_ValidImageFile_ReturnsParsedData() throws IOException {
        // given
        when(clovaOcrClient.extractTextFromImage(any())).thenReturn(sampleOcrResponseJson);

        // when
        PrescriptionParsedDTO result = medicationService.processPrescription(mockImageFile);

        // then
        assertNotNull(result);
        assertEquals(2, result.getMedications().size());

        // 첫 번째 약물 검증
        PrescriptionParsedDTO.MedicationInfo firstMed = result.getMedications().get(0);
        assertEquals("Amoxicillin (항생제)", firstMed.getDrugName());
        assertEquals("50mg", firstMed.getDosage());
        assertEquals("경구투여, 하루 2회", firstMed.getAdministration());
        assertEquals("하루 2회", firstMed.getFrequency());
        assertEquals("3일", firstMed.getPrescriptionDays());

        // 두 번째 약물 검증
        PrescriptionParsedDTO.MedicationInfo secondMed = result.getMedications().get(1);
        assertEquals("Firocoxib (소염진통제)", secondMed.getDrugName());
        assertEquals("57mg", secondMed.getDosage());
        assertEquals("경구투여, 하루 1회", secondMed.getAdministration());
        assertEquals("하루 1회", secondMed.getFrequency());
        assertEquals("7일", secondMed.getPrescriptionDays());
    }

    @Test
    void parsePrescription_ValidOcrResponse_ReturnsParsedData() throws IOException {
        // when
        PrescriptionParsedDTO result = medicationService.parsePrescription(sampleOcrResponseJson);

        // then
        assertNotNull(result);
        assertEquals(2, result.getMedications().size());
    }

    @Test
    void processOcrImage_ValidImageFile_ReturnsOcrText() throws IOException {
        // given
        String expectedOcrText = "OCR 결과 텍스트";
        when(clovaOcrClient.extractTextFromImage(any())).thenReturn(expectedOcrText);

        // when
        String result = medicationService.processOcrImage(mockImageFile);

        // then
        assertEquals(expectedOcrText, result);
    }
}
