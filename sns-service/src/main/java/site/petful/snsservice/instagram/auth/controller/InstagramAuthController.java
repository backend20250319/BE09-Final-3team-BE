package site.petful.snsservice.instagram.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.batch.service.InstagramBatchService;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.auth.dto.InstagramConnectRequestDto;
import site.petful.snsservice.instagram.auth.service.InstagramAuthService;

@RestController
@RequestMapping("/instagram/auth")
@RequiredArgsConstructor
public class InstagramAuthController {

    private final InstagramAuthService instagramAuthService;
    private final InstagramBatchService instagramBatchService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<String>> connectInstagram(
        @AuthenticationPrincipal String userNo,
        @Valid @RequestBody InstagramConnectRequestDto dto) {

        String accessToken = dto.accessToken();
        String encryptedToken = instagramAuthService.connect(Long.valueOf(userNo), accessToken);

        instagramBatchService.runInstagramSyncBatchForUserAsync(Long.valueOf(userNo), 6L);

        return ResponseEntity.ok(ApiResponseGenerator.success(encryptedToken));
    }

}
