package site.petful.advertiserservice.login.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.login.dto.LoginRequest;
import site.petful.advertiserservice.login.dto.LoginResponse;
import site.petful.advertiserservice.login.service.LoginService;

@RestController
@RequestMapping("/advertiser")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = loginService.login(request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, e.getMessage()));
        }
    }
}

