package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    private Long historyNo;            // 활동 이력 번호
    private LocalDate historyStart;    // 활동 시작일
    private LocalDate historyEnd;      // 활동 종료일
    private String content;            // 활동 내용
    private Long petNo;                // 반려동물 번호
    private LocalDateTime createdAt;   // 생성일
    private LocalDateTime updatedAt;   // 수정일
}
