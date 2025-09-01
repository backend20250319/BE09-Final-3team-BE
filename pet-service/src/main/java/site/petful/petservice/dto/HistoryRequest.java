package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRequest {
    private LocalDate historyStart;    // 활동 시작일
    private LocalDate historyEnd;      // 활동 종료일
    private String content;            // 활동 내용
    private String title;
    private List<String> image_urls;   // 이미지 URL 목록 (수정 시 기존 이미지 유지용)
}
