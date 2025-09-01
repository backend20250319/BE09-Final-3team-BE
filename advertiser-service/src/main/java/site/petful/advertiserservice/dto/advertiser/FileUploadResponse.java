package site.petful.advertiserservice.dto.advertiser;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.entity.advertiser.AdvertiserFiles;
import site.petful.advertiserservice.entity.advertiser.FileType;

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

    public static FileUploadResponse from(AdvertiserFiles files) {
        FileUploadResponse response = new FileUploadResponse();
        response.setOriginalName(files.getOriginalName());
        response.setSavedName(files.getSavedName());
        response.setFilePath(files.getFilePath());
        response.setIsDeleted(files.getIsDeleted());
        response.setCreatedAt(files.getCreatedAt());
        response.setType(files.getType());
        response.setAdvertiserNo(files.getAdvertiser().getAdvertiserNo());

        return response;
    }
}


