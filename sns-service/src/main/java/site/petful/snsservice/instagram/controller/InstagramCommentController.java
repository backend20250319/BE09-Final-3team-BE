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
import site.petful.snsservice.instagram.dto.InstagramCommentDto;
import site.petful.snsservice.instagram.service.InstagramCommentService;

@RestController
@RequestMapping("/instagram/comments")
@RequiredArgsConstructor
public class InstagramCommentController {

    private final InstagramCommentService instagramCommentService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<InstagramCommentDto>>> syncInstagramComments(
        @RequestParam Long userId, @RequestParam Long mediaId) {

        List<InstagramCommentDto> comments = instagramCommentService.syncInstagramCommentByUserIdAndMediaId(
            userId,
            mediaId);
        return ResponseEntity.ok(ApiResponseGenerator.success(comments));
    }
}
