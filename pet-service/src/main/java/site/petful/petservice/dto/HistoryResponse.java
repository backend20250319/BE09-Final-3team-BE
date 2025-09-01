package site.petful.petservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    
    private String title;              // 활동 제목
    private String content;            // 활동 내용
    private List<String> imageUrls;    // 이미지 URL 목록
    private Long petNo;                // 반려동물 번호
    private List<String> imageUrls;    // 이미지 URL 목록
    private LocalDateTime createdAt;   // 생성일
    private LocalDateTime updatedAt;   // 수정일
}
