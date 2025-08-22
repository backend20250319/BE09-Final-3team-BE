package site.petful.mypageservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.mypageservice.dto.ApiResponse;
import site.petful.mypageservice.dto.CompleteUserProfileResponse;
import site.petful.mypageservice.dto.UserProfileRequest;
import site.petful.mypageservice.service.UserProfileService;
import site.petful.mypageservice.util.JwtUtil;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
// @CrossOrigin(origins = "*") // CORS는 Gateway에서 처리
public class UserProfileController {
    
    private final UserProfileService userProfileService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CompleteUserProfileResponse>> getProfile(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            String email = jwtUtil.extractEmail(token);
            
            CompleteUserProfileResponse profile = userProfileService.getCompleteUserProfile(email);
            return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CompleteUserProfileResponse>> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UserProfileRequest profileRequest) {
        try {
            String token = extractTokenFromRequest(request);
            String email = jwtUtil.extractEmail(token);
            
            userProfileService.updateUserProfile(email, profileRequest);
            CompleteUserProfileResponse updatedProfile = userProfileService.getCompleteUserProfile(email);
            return ResponseEntity.ok(ApiResponse.success("프로필 수정 성공", updatedProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("유효한 토큰이 없습니다");
    }
}
