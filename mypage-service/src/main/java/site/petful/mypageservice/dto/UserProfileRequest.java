package site.petful.mypageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    
    private String profileImageUrl;
    
    private String selfIntroduction;
    
    private String instagramUsername;
    
    private String preferredPetType;
    
    private Integer petCount;
    
    private Boolean isInfluencer;
    
    private String influencerCategory;
}
