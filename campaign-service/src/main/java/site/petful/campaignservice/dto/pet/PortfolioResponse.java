package site.petful.campaignservice.dto.pet;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.dto.HistoryResponse;

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
