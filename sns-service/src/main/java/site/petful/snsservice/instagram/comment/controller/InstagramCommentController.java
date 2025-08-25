package site.petful.snsservice.instagram.comment.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.petful.snsservice.common.ApiResponse;
import site.petful.snsservice.common.ApiResponseGenerator;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentResponseDto;
import site.petful.snsservice.instagram.comment.service.InstagramBannedWordService;
import site.petful.snsservice.instagram.comment.service.InstagramCommentService;

@RestController
@RequestMapping("/instagram/comments")
@RequiredArgsConstructor
public class InstagramCommentController {

    private final InstagramTokenService instagramTokenService;
    private final InstagramCommentService instagramCommentService;
    private final InstagramBannedWordService instagramBannedWordService;


    //TODO 게시물별 댓글 조회
    // instagramId 별 모든 댓글 조회 paging이 들어가야함ㄴ
    // 총 댓글수, 자동삭제, 삭제 비율, 금지어 개수 get
    @GetMapping
    public ResponseEntity<ApiResponse<List<InstagramCommentResponseDto>>> getInstagramComments(
        @RequestParam Long userNo, @RequestParam Long mediaId) {

        List<InstagramCommentResponseDto> comments = instagramCommentService.getComments(
            mediaId);
        return ResponseEntity.ok(ApiResponseGenerator.success(comments));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteInstagramComment(
        @RequestParam(name = "user_no") Long userNo,
        @RequestParam(name = "comment_id") Long commentId) {
        String accessToken = instagramTokenService.getAccessToken(userNo);

        instagramCommentService.deleteComment(commentId, accessToken);
        return ResponseEntity.ok(ApiResponseGenerator.success(null));

    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<InstagramCommentResponseDto>>> syncInstagramComments(
        @RequestParam(name = "user_no") Long userNo, @RequestParam Long mediaId) {

        String accessToken = instagramTokenService.getAccessToken(userNo);
        List<InstagramCommentResponseDto> comments = instagramCommentService.syncInstagramCommentByMediaId(

            mediaId, accessToken);
        return ResponseEntity.ok(ApiResponseGenerator.success(comments));
    }

    @PostMapping("/banned-words")
    public ResponseEntity<ApiResponse<Void>> addBannedWord(
        @RequestParam(name = "instagram_id") Long instagramId,
        @RequestParam(name = "word") String word) {

        instagramBannedWordService.addBannedWord(instagramId, word);
        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

    @DeleteMapping("/banned-words")
    public ResponseEntity<ApiResponse<Void>> deleteBannedWord(
        @RequestParam(name = "instagram_id") Long instagramId,
        @RequestParam(name = "word") String word) {

        instagramBannedWordService.deleteBannedWord(instagramId, word);
        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }
}
