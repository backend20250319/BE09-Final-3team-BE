package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryImageInfo {
    private Long id;              // 이미지 ID (DB의 id 컬럼)
    private String url;           // 이미지 URL (filePath)
    private String originalName;  // 원본 파일명
    private String savedName;     // 저장된 파일명
    private Long historyNo;       // 활동 이력 번호
}

