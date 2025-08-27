package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRequest {
    private LocalDate historyStart;    // 활동 시작일
    private LocalDate historyEnd;      // 활동 종료일
    private String content;            // 활동 내용
}
