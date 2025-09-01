package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipleFileUploadResponse {
    private boolean success;
    private String message;
    private List<String> fileUrls;
    private int uploadedCount;
}

