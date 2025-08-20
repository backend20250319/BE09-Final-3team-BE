package site.petful.userservice.service;

import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.SignupResponse;
import site.petful.userservice.dto.TokenValidationResponse;

public interface UserService {
    SignupResponse signup(SignupRequest request);   // ✅ AuthResponse → SignupResponse
    void markEmailVerified(String email);
    TokenValidationResponse validateToken(String token);
}
