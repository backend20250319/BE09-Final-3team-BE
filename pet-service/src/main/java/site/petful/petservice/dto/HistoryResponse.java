package site.petful.petservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import site.petful.petservice.dto.HistoryImageInfo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    private Long historyNo;            // 활동 이력 번호
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate historyStart;    // 활동 시작일
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate historyEnd;      // 활동 종료일
    
    private String title;              // 제목
    private String content;            // 내용
    
    @Deprecated // 기존 호환성을 위해 유지, 향후 제거 예정
    private List<String> imageUrls;    // 이미지 URL 목록 (기존)
    
    private List<HistoryImageInfo> images; // 이미지 상세 정보 (새로 추가)
    
    private Long petNo;                // 반려동물 번호
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;   // 생성일시
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;   // 수정일시
}
