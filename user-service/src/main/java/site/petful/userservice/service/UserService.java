package site.petful.userservice.service;

import site.petful.userservice.dto.AuthResponse;
import site.petful.userservice.dto.LoginRequest;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.TokenValidationResponse;

public interface UserService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    void markEmailVerified(String email);
    TokenValidationResponse validateToken(String token);
}
