package site.petful.advertiserservice.dto.advertiser;

import lombok.Getter;

@Getter
public class FileMetaUpdateRequest {

    private Boolean isDeletedForFile;
    private Boolean isDeletedForImage;
}
