package site.petful.petservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.petful.petservice.entity.PetStarStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {

    private Long petNo;
    private Long userNo;
    private String name;
    private String type;
    private String imageUrl;
    private String snsUrl;
    private Long age;
    private String gender;
    private Float weight;
    private Boolean isPetStar;
    private Long snsId;
    private Long snsProfileNo;
    private String snsUsername;
    private PetStarStatus petStarStatus;
    private LocalDateTime pendingAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Instagram 프로필 정보 (연결된 경우에만)
    private InstagramProfileInfo instagramProfile;
}
