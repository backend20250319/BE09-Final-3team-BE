package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ImageUploadResponse {

    private String originalName;
    private String savedName;
    private String filePath;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private Long adNo;
}
