package site.petful.mypageservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.mypageservice.dto.CompleteUserProfileResponse;
import site.petful.mypageservice.dto.UserProfileRequest;
import site.petful.mypageservice.entity.User;
import site.petful.mypageservice.entity.UserBasic;
import site.petful.mypageservice.repository.UserBasicRepository;
import site.petful.mypageservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    
    private final UserRepository userRepository;
    private final UserBasicRepository userBasicRepository;
    
    public CompleteUserProfileResponse getCompleteUserProfile(String email) {
        // user-service의 users 테이블에서 기본 정보 가져오기
        UserBasic userBasic = userBasicRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        
        // 마이페이지 전용 정보 가져오기
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createDefaultProfile(email));
        
        // 두 정보를 합쳐서 반환
        return CompleteUserProfileResponse.builder()
                // 기본 정보 (user-service와 동일)
                .userNo(userBasic.getUserNo())
                .email(userBasic.getEmail())
                .name(userBasic.getName())
                .nickname(userBasic.getNickname())
                .phone(userBasic.getPhone())
                .userType(userBasic.getUserType())
                .birthDate(userBasic.getBirthDate())
                .description(userBasic.getDescription())
                .roadAddress(userBasic.getRoadAddress())
                .detailAddress(userBasic.getDetailAddress())
                .address(userBasic.getAddress())
                .detailedAddress(userBasic.getDetailedAddress())
                .birthYear(userBasic.getBirthYear())
                .birthMonth(userBasic.getBirthMonth())
                .birthDay(userBasic.getBirthDay())
                .emailVerified(userBasic.getEmailVerified())
                .createdAt(userBasic.getCreatedAt())
                .updatedAt(userBasic.getUpdatedAt())
                .isActive(userBasic.getIsActive())
                .imageNo(userBasic.getImageNo())
                // 마이페이지 정보
                .profileId(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .selfIntroduction(user.getSelfIntroduction())
                .instagramUsername(user.getInstagramUsername())
                .instagramConnected(user.getInstagramConnected())
                .preferredPetType(user.getPreferredPetType())
                .petCount(user.getPetCount())
                .isInfluencer(user.getIsInfluencer())
                .influencerCategory(user.getInfluencerCategory())
                .profileCreatedAt(user.getCreatedAt())
                .profileUpdatedAt(user.getUpdatedAt())
                .build();
    }
    
    public void updateUserProfile(String email, UserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createDefaultProfile(email));
        
        // 프로필 정보 업데이트
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getSelfIntroduction() != null) {
            user.setSelfIntroduction(request.getSelfIntroduction());
        }
        if (request.getInstagramUsername() != null) {
            user.setInstagramUsername(request.getInstagramUsername());
        }
        if (request.getPreferredPetType() != null) {
            user.setPreferredPetType(request.getPreferredPetType());
        }
        if (request.getPetCount() != null) {
            user.setPetCount(request.getPetCount());
        }
        if (request.getIsInfluencer() != null) {
            user.setIsInfluencer(request.getIsInfluencer());
        }
        if (request.getInfluencerCategory() != null) {
            user.setInfluencerCategory(request.getInfluencerCategory());
        }
        
        userRepository.save(user);
    }
    
    private User createDefaultProfile(String email) {
        User user = User.builder()
                .email(email)
                .profileImageUrl(null)
                .selfIntroduction(null)
                .instagramUsername(null)
                .instagramConnected(false)
                .preferredPetType(null)
                .petCount(0)
                .isInfluencer(false)
                .influencerCategory(null)
                .build();
        
        return userRepository.save(user);
    }
}
