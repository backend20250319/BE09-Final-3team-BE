package site.petful.campaignservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class HistoryResponse {
    private Long historyNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate historyStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate historyEnd;

    private String title;
    private String content;
    private List<String> imageUrls;
    private Long petNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

