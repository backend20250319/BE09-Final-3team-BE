package site.petful.snsservice.instagram.media.controller;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.media.dto.InstagramMediaDto;
import site.petful.snsservice.instagram.media.service.InstagramMediaService;

@RestController
@RequestMapping("/instagram/medias")
@RequiredArgsConstructor
public class InstagramMediaController {

    private final InstagramMediaService instagramMediaService;
    private final InstagramTokenService instagramTokenService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<List<InstagramMediaDto>>> syncMedias(
        @RequestParam(name = "user_no") Long userNo,
        @RequestParam(name = "instagram_id") Long instagramId) {

        String accessToken = instagramTokenService.getAccessToken(userNo);
        List<InstagramMediaDto> mediaList = instagramMediaService.syncInstagramMedia(instagramId,
            accessToken);
        return ResponseEntity.ok(ApiResponseGenerator.success(mediaList));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<InstagramMediaDto>>> getMedias(
        @NotNull @RequestParam(name = "instagram_id") Long instagramId) {
        List<InstagramMediaDto> mediasDto = instagramMediaService.getMedias(instagramId);
        return ResponseEntity.ok(ApiResponseGenerator.success(mediasDto));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<InstagramMediaDto>> getMedia(
        @NotNull @PathVariable Long mediaId) {

        InstagramMediaDto mediaDto = instagramMediaService.getMedia(mediaId);

        return ResponseEntity.ok(ApiResponseGenerator.success(mediaDto));
    }
}
