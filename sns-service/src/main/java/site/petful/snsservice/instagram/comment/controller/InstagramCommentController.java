package site.petful.snsservice.instagram.comment.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import site.petful.snsservice.instagram.comment.dto.BannedWordResponseDto;
import site.petful.snsservice.instagram.comment.dto.CommentSentimentRatioResponseDto;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentResponseDto;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentStatusResponseDto;
import site.petful.snsservice.instagram.comment.entity.Sentiment;
import site.petful.snsservice.instagram.comment.service.InstagramBannedWordService;
import site.petful.snsservice.instagram.comment.service.InstagramCommentService;

@RestController
@RequestMapping("/instagram/comments")
@RequiredArgsConstructor
public class InstagramCommentController {

    private final InstagramTokenService instagramTokenService;
    private final InstagramCommentService instagramCommentService;
    private final InstagramBannedWordService instagramBannedWordService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<InstagramCommentResponseDto>>> searchInstagramComments(
        @RequestParam(name = "instagram_id") Long instagramId,
        @RequestParam(name = "is_deleted", required = false) Boolean isDeleted,
        @RequestParam(required = false) Sentiment sentiment,
        @RequestParam(required = false) String keyword,
        @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        // 서비스 계층에 검색 조건과 페이징 정보를 전달
        Page<InstagramCommentResponseDto> commentPage = instagramCommentService.searchComments(
            instagramId, isDeleted, sentiment, keyword, pageable);

        return ResponseEntity.ok(ApiResponseGenerator.success(commentPage));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstagramCommentResponseDto>>> getInstagramComments(
        @RequestParam(name = "user_no") Long userNo,
        @RequestParam(name = "media_id") Long mediaId) {

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


    @PostMapping("/banned-words")
    public ResponseEntity<ApiResponse<Void>> addBannedWord(
        @RequestParam(name = "instagram_id") Long instagramId,
        @RequestParam(name = "word") String word) {

        instagramBannedWordService.addBannedWord(instagramId, word);
        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

    @GetMapping("/banned-words")
    public ResponseEntity<ApiResponse<List<BannedWordResponseDto>>> getBannedWords(
        @RequestParam(name = "instagram_id") Long instagramId,
        @RequestParam(required = false) String keyword) {

        List<BannedWordResponseDto> bannedWords = instagramBannedWordService.getBannedWords(
            instagramId, keyword);
        return ResponseEntity.ok(ApiResponseGenerator.success(bannedWords));
    }

    @DeleteMapping("/banned-words")
    public ResponseEntity<ApiResponse<Void>> deleteBannedWord(
        @RequestParam(name = "instagram_id") Long id, @RequestParam String word) {

        instagramBannedWordService.deleteBannedWord(id, word);
        return ResponseEntity.ok(ApiResponseGenerator.success(null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<InstagramCommentStatusResponseDto>> getStatus(
        @RequestParam(name = "instagram_id") Long instagramId) {
        InstagramCommentStatusResponseDto status = instagramCommentService.getCommentStatus(
            instagramId);
        return ResponseEntity.ok(ApiResponseGenerator.success(status));

    }

    @GetMapping("/sentiment-ratio")
    public ResponseEntity<ApiResponse<CommentSentimentRatioResponseDto>> getSentimentRatio(
        @RequestParam(name = "instagram_id") Long instagramId) {
        CommentSentimentRatioResponseDto sentimentRatio = instagramCommentService.getSentimentRatio(
            instagramId);
        return ResponseEntity.ok(ApiResponseGenerator.success(sentimentRatio));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<InstagramCommentResponseDto>>> syncInstagramComments(
        @RequestParam(name = "user_no") Long userNo,
        @RequestParam(name = "media_id") Long mediaId) {

        String accessToken = instagramTokenService.getAccessToken(userNo);
        List<InstagramCommentResponseDto> comments = instagramCommentService.syncInstagramCommentByMediaId(

            mediaId, accessToken);
        return ResponseEntity.ok(ApiResponseGenerator.success(comments));
    }

}
