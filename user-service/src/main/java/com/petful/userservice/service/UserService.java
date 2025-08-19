package com.petful.userservice.service;

import com.petful.userservice.dto.AuthResponse;
import com.petful.userservice.dto.LoginRequest;
import com.petful.userservice.dto.SignupRequest;
import com.petful.userservice.dto.TokenValidationResponse;

public interface UserService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    void markEmailVerified(String email);
    TokenValidationResponse validateToken(String token);
}
