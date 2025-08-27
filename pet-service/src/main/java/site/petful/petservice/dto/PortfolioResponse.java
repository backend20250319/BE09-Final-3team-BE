package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private Long petNo;
    private String content;
    private Long cost;
    private String contact;
    private Boolean isSaved;
    private String personality;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
