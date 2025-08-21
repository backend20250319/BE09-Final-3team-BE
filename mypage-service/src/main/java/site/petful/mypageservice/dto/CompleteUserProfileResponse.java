package site.petful.mypageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.petful.mypageservice.entity.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteUserProfileResponse {
    
    // 기본 사용자 정보 (user-service와 동일)
    private Long userNo;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private Role userType;
    private LocalDate birthDate;
    private String description;
    private String roadAddress;
    private String detailAddress;
    private String address;
    private String detailedAddress;
    private Integer birthYear;
    private Integer birthMonth;
    private Integer birthDay;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Long imageNo;
    
    // 마이페이지 전용 정보
    private Long profileId;
    private String profileImageUrl;
    private String selfIntroduction;
    private String instagramUsername;
    private Boolean instagramConnected;
    private String preferredPetType;
    private Integer petCount;
    private Boolean isInfluencer;
    private String influencerCategory;
    private LocalDateTime profileCreatedAt;
    private LocalDateTime profileUpdatedAt;
}
