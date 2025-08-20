package site.petful.snsservice.instagram.controller;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponse;
import site.petful.snsservice.instagram.service.InstagramService;

@RestController
@RequestMapping("/instagram")
@RequiredArgsConstructor
public class InstagramController {

    private final InstagramService instagramService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<String>> connectInstagram(@RequestParam String token) {
        // TODO [유저] 정보도 가져와야됌 쭉 들어가면서 수정
        String encryptedToken = instagramService.connect(token);
        return ResponseEntity.ok(ApiResponseGenerator.success(encryptedToken));
    }

    //TODO [유저] 여기 userId 수정 쭉 들어가면서
    @PostMapping("/profiles")
    public ResponseEntity<ApiResponse<List<InstagramProfileResponse>>> syncProfiles(
        @RequestParam Long userId) {
        return ResponseEntity.ok(
            ApiResponseGenerator.success(instagramService.syncInstagramProfiles(userId)));
    }


/*
    //TODO [유저] 여기 userId 수정 쭉 들어가면서
    @PostMapping("/profiles/{instagramId}")
    public ResponseEntity<ApiResponse<List<String>>> syncProfile(@PathVariable Long instagramId,
        @RequestParam Long userId) {
        return ResponseEntity.ok(
            ApiResponseGenerator.success(
                instagramService.syncInstagramProfile(userId, instagramId)));
    }
*/

}