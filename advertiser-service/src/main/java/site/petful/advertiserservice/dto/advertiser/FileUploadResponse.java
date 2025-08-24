package site.petful.advertiserservice.dto.advertiser;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {
    private String filename;
    private String message;
    private boolean success;
}

