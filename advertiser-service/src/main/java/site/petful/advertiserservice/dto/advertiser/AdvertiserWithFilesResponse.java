package site.petful.advertiserservice.dto.advertiser;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.entity.advertiser.Advertiser;
import site.petful.advertiserservice.entity.advertiser.AdvertiserFiles;
import site.petful.advertiserservice.entity.advertiser.FileType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdvertiserWithFilesResponse {
    private Long advertiserNo;
    private String name;
    private String email;
    private String businessNumber;
    private String phone;
    private String website;
    private String reason;
    private LocalDateTime createdAt;
    
    // 파일 정보
    private String profileImageUrl;  // PROFILE 타입 파일의 URL
    private String documentUrl;      // DOC 타입 파일의 URL
    private String profileOriginalName;  // 프로필 파일 원본명
    private String documentOriginalName; // 문서 파일 원본명

    public static AdvertiserWithFilesResponse from(Advertiser advertiser, List<AdvertiserFiles> files) {
        AdvertiserWithFilesResponse response = new AdvertiserWithFilesResponse();
        response.setAdvertiserNo(advertiser.getAdvertiserNo());
        response.setName(advertiser.getName());
        response.setEmail(advertiser.getEmail());
        response.setBusinessNumber(advertiser.getBusinessNumber());
        response.setPhone(advertiser.getPhone());
        response.setWebsite(advertiser.getWebsite());
        response.setReason(advertiser.getReason());
        response.setCreatedAt(advertiser.getCreatedAt());
        
        // 파일 정보 설정
        if (files != null && !files.isEmpty()) {
            // 프로필 사진 찾기
            AdvertiserFiles profileFile = files.stream()
                    .filter(file -> file.getType() == FileType.PROFILE)
                    .findFirst()
                    .orElse(null);
            
            if (profileFile != null) {
                response.setProfileImageUrl(profileFile.getFilePath());
                response.setProfileOriginalName(profileFile.getOriginalName());
            }
            
            // 문서 찾기
            AdvertiserFiles documentFile = files.stream()
                    .filter(file -> file.getType() == FileType.DOC)
                    .findFirst()
                    .orElse(null);
            
            if (documentFile != null) {
                response.setDocumentUrl(documentFile.getFilePath());
                response.setDocumentOriginalName(documentFile.getOriginalName());
            }
        }
        
        return response;
    }
}
