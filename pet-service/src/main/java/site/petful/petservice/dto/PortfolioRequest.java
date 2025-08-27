package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRequest {
    private String content;
    private Long cost;
    private String contact;
    private Boolean isSaved;
    private String personality;
}
