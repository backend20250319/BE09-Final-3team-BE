package site.petful.userservice.service;

import site.petful.userservice.domain.User;
import site.petful.userservice.dto.ProfileResponse;
import site.petful.userservice.dto.ProfileUpdateRequest;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.SignupResponse;

public interface UserService {
    SignupResponse signup(SignupRequest request);   // ✅ AuthResponse → SignupResponse
    void markEmailVerified(String email);
    User findByEmail(String email);
    
    // 프로필 관련 메서드들
    ProfileResponse getProfile(Long userNo);
    ProfileResponse updateProfile(Long userNo, ProfileUpdateRequest request);
}
