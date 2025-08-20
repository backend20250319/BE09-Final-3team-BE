package site.petful.userservice.service;

import site.petful.userservice.domain.User;
import site.petful.userservice.dto.SignupRequest;
import site.petful.userservice.dto.SignupResponse;

public interface UserService {
    SignupResponse signup(SignupRequest request);   // ✅ AuthResponse → SignupResponse
    void markEmailVerified(String email);
    User findByEmail(String email);
}
