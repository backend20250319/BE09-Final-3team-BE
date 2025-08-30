package site.petful.advertiserservice.dto.advertisement;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.entity.advertisement.AdFiles;

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

    public static ImageUploadResponse from(AdFiles adFiles) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setOriginalName(adFiles.getOriginalName());
        response.setSavedName(adFiles.getSavedName());
        response.setFilePath(adFiles.getFilePath());
        response.setIsDeleted(adFiles.getIsDeleted());
        response.setCreatedAt(adFiles.getCreatedAt());
        response.setAdNo(adFiles.getAdvertisement().getAdNo());

        return response;
    }
}
