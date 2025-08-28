package site.petful.userservice.service;

import org.springframework.web.multipart.MultipartFile;
import site.petful.userservice.entity.User;
import site.petful.userservice.dto.PasswordChangeRequest;
import site.petful.userservice.dto.PasswordResetRequest;
import site.petful.userservice.dto.PasswordResetResponse;
import site.petful.userservice.dto.VerificationConfirmRequest;
import site.petful.userservice.dto.VerificationConfirmResponse;
import site.petful.userservice.dto.FileUploadResponse;
import site.petful.userservice.dto.ProfileResponse;
import site.petful.userservice.dto.ProfileUpdateRequest;
import site.petful.userservice.dto.SimpleProfileResponse;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.SignupResponse;

public interface UserService {
    SignupResponse signup(SignupRequest request);   // ✅ AuthResponse → SignupResponse
    void markEmailVerified(String email);
    User findByEmail(String email);
    
    // 프로필 관련 메서드들
    ProfileResponse getProfile(Long userNo);
    ProfileResponse updateProfile(Long userNo, ProfileUpdateRequest request);
    SimpleProfileResponse getSimpleProfile(Long userNo);
    
    // 비밀번호 재설정 관련 메서드들
    PasswordResetResponse requestPasswordReset(PasswordResetRequest request);
    VerificationConfirmResponse verifyPasswordResetCode(VerificationConfirmRequest request);
    void changePassword(PasswordChangeRequest request);
    
    // 파일 업로드 관련 메서드들
    FileUploadResponse uploadProfileImage(MultipartFile file, Long userNo);
}
