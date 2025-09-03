package site.petful.campaignservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PortfolioResponse {
    private Long petNo;
    private String content;
    private Long cost;
    private String contact;
    private Boolean isSaved;
    private String personality;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<HistoryResponse> histories;
}
