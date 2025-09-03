package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.petful.petservice.entity.PortfolioImageFile;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioImageFileResponse {

    private Long id;
    private String originalName;
    private String savedName;
    private String filePath;
    private String viewUrl; // FTP view-url과 결합된 웹 접근 URL
    private LocalDateTime createdAt;
    private Long historyNo;

    public static PortfolioImageFileResponse from(PortfolioImageFile portfolioImageFile, String viewUrl) {
        return PortfolioImageFileResponse.builder()
                .id(portfolioImageFile.getId())
                .originalName(portfolioImageFile.getOriginalName())
                .savedName(portfolioImageFile.getSavedName())
                .filePath(portfolioImageFile.getFilePath())
                .viewUrl(viewUrl + "/" + portfolioImageFile.getSavedName())
                .createdAt(portfolioImageFile.getCreatedAt())
                .historyNo(portfolioImageFile.getHistoryNo())
                .build();
    }
}
