package site.petful.campaignservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.FileType;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileUploadResponse {
    private String originalName;
    private String savedName;
    private String filePath;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private FileType type;
    private Long advertiserNo;
}
