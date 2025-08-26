package site.petful.snsservice.instagram.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.auth.dto.InstagramConnectRequestDto;
import site.petful.snsservice.instagram.auth.service.InstagramAuthService;

@RestController
@RequestMapping("/instagram/auth")
@RequiredArgsConstructor
public class InstagramAuthController {

    private final InstagramAuthService instagramAuthService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<String>> connectInstagram(
        @RequestBody InstagramConnectRequestDto dto) {
        // TODO [유저] 정보도 가져와야됌 쭉 들어가면서 수정 userNo로 저장
        Long userNo = 1L;
        String accessToken = dto.accessToken();

        String encryptedToken = instagramAuthService.connect(userNo, accessToken);
        return ResponseEntity.ok(ApiResponseGenerator.success(encryptedToken));
    }

    /*// TODO [추후에 시간 남으면]
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") String hubMode,
        @RequestParam("hub.verify_token") String hubVerifyToken,
        @RequestParam("hub.challenge") String hubChallenge) {

        System.out.println("hub_mode = " + hubMode);
        System.out.println("hub_verify_token = " + hubVerifyToken);
        System.out.println("hub_challenge = " + hubChallenge);

        return ResponseEntity.ok(hubChallenge);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhookEvents(
        @RequestBody String payload) {
        System.out.println(payload);

        return ResponseEntity.ok(null);
    }*/
}
