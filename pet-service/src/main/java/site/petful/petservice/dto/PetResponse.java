package site.petful.petservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.petful.petservice.entity.PetStarStatus;

import java.time.LocalDateTime;

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
    private Long snsProfileNo;
    private PetStarStatus petStarStatus;
    private LocalDateTime pendingAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
