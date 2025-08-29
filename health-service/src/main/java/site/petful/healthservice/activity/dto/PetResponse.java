package site.petful.healthservice.activity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResponse {
    
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private PetData data;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PetData {
        @JsonProperty("petNo")
        private Long petNo;
        
        @JsonProperty("userNo")
        private Long userNo;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("age")
        private Integer age;
        
        @JsonProperty("gender")
        private String gender;
        
        @JsonProperty("weight")
        private Double weight;
        
        @JsonProperty("imageUrl")
        private String imageUrl;
        
        @JsonProperty("isPetStar")
        private Boolean isPetStar;
        
        @JsonProperty("snsProfileNo")
        private Long snsProfileNo;
        
        @JsonProperty("petStarStatus")
        private String petStarStatus;
        
        @JsonProperty("pendingAt")
        private String pendingAt;
        
        @JsonProperty("createdAt")
        private String createdAt;
        
        @JsonProperty("updatedAt")
        private String updatedAt;
    }
}
