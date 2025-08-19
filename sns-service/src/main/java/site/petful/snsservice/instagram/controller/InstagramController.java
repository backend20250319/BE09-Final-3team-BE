package site.petful.snsservice.instagram.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.service.InstagramService;

@RestController
@RequestMapping("/instagram")
@RequiredArgsConstructor
public class InstagramController {

    private final InstagramService instagramService;

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<Void>> connectInstagram(@RequestParam String token) {
        // TODO : 유저 정보도 가져와야됌

        instagramService.connect(token);
        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

    @DeleteMapping("/connect")
    public ResponseEntity<ApiResponse<Void>> disconnectInstagram(@RequestParam Long userId) {

        return ResponseEntity.ok(ApiResponseGenerator.success());
    }
}