package site.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    
    private String fileName;
    private String fileUrl;
    private String message;
    private boolean success;
    private ProfileResponse profileResponse;  // 업데이트된 프로필 정보
    
}
