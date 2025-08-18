package com.petful.userservice.service;

import com.petful.userservice.dto.AuthResponse;
import com.petful.userservice.dto.LoginRequest;
import com.petful.userservice.dto.SignupRequest;

public interface UserService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
}
