package site.petful.snsservice.instagram.media.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.media.dto.InstagramMediaDto;
import site.petful.snsservice.instagram.media.service.InstagramMediaService;

@RestController
@RequestMapping("/instagram/media")
@RequiredArgsConstructor
public class InstagramMediaController {

    private final InstagramMediaService instagramMediaService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<InstagramMediaDto>>> syncInstagramMedia(
        @RequestParam Long userId, @RequestParam Long instagramId) {

        List<InstagramMediaDto> mediaList = instagramMediaService.syncInstagramMedia(userId,
            instagramId);
        return ResponseEntity.ok(ApiResponseGenerator.success(mediaList));
    }
}
