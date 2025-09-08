package site.petful.communityservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.common.ErrorCode;
import site.petful.communityservice.dto.CommentCreateRequest;
import site.petful.communityservice.dto.CommentUpdateRequest;
import site.petful.communityservice.dto.CommentPageDto;
import site.petful.communityservice.dto.CommentView;
import site.petful.communityservice.service.CommentService;

import org.springframework.security.access.AccessDeniedException;

@Slf4j
@RestController
@RequestMapping("community/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<CommentPageDto>> getComment(
            @PathVariable Long postId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("💬 [CommentController] 댓글 목록 조회: postId={}, page={}, size={}", 
                postId, pageable.getPageNumber(), pageable.getPageSize());
        
        // 파라미터 유효성 검증
        if (postId == null || postId <= 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 게시글 ID: {}", postId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_POST_ID, (CommentPageDto) null));
        }
        
        // 페이지 파라미터 유효성 검증
        if (pageable.getPageNumber() < 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 페이지 번호: {}", pageable.getPageNumber());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (CommentPageDto) null));
        }
        
        if (pageable.getPageSize() <= 0 || pageable.getPageSize() > 100) {
            log.warn("⚠️ [CommentController] 유효하지 않은 페이지 크기: {}", pageable.getPageSize());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (CommentPageDto) null));
        }
        
        try {
            CommentPageDto result = commentService.listComments(postId, pageable);
            log.info("✅ [CommentController] 댓글 목록 조회 성공: postId={}", postId);
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (IllegalArgumentException e) {
            log.error("❌ [CommentController] 잘못된 파라미터: postId={}, error={}", postId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (CommentPageDto) null));
        } catch (RuntimeException e) {
            log.error("❌ [CommentController] 댓글 목록 조회 실패: postId={}, error={}", postId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.OPERATION_FAILED, (CommentPageDto) null));
        } catch (Exception e) {
            log.error("❌ [CommentController] 예상치 못한 오류: postId={}, error={}", postId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (CommentPageDto) null));
        }
    }


    @PostMapping("/insert")
    public ResponseEntity<ApiResponse<CommentView>> create(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @RequestBody CommentCreateRequest request
    ) {
        log.info("💬 [CommentController] 댓글 생성: userId={}", userNo);
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [CommentController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (CommentView) null));
        }
        
        // 사용자 ID 유효성 검증
        if (userNo <= 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (CommentView) null));
        }
        
        // 요청 데이터 유효성 검증
        if (request == null) {
            log.warn("⚠️ [CommentController] 요청 데이터가 null입니다");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (CommentView) null));
        }
        
        if (request.getPostId() == null || request.getPostId() <= 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 게시글 ID: {}", request.getPostId());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_POST_ID, (CommentView) null));
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            log.warn("⚠️ [CommentController] 댓글 내용이 비어있습니다");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_CONTENT, (CommentView) null));
        }
        
        try {
            CommentView response = commentService.createComment(userNo, request);
            log.info("✅ [CommentController] 댓글 생성 성공: userId={}, postId={}", userNo, request.getPostId());
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (IllegalArgumentException e) {
            log.error("❌ [CommentController] 잘못된 파라미터: userId={}, error={}", userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (CommentView) null));
        } catch (RuntimeException e) {
            log.error("❌ [CommentController] 댓글 생성 실패: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.COMMENT_CREATE_FAILED, (CommentView) null));
        } catch (Exception e) {
            log.error("❌ [CommentController] 예상치 못한 오류: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (CommentView) null));
        }
    }

    @PatchMapping("/{commentId}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PathVariable Long commentId
    ) {
        log.info("🗑️ [CommentController] 댓글 삭제: userId={}, commentId={}", userNo, commentId);
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [CommentController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }
        
        // 파라미터 유효성 검증
        if (commentId == null || commentId <= 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 댓글 ID: {}", commentId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_COMMENT_ID, (Void) null));
        }
        
        if (userNo <= 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        }
        
        try {
            commentService.deleteComment(userNo, commentId, userType);
            log.info("✅ [CommentController] 댓글 삭제 성공: userId={}, commentId={}", userNo, commentId);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (IllegalArgumentException e) {
            log.error("❌ [CommentController] 잘못된 파라미터: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        } catch (AccessDeniedException e) {
            log.error("❌ [CommentController] 접근 권한 없음: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage());
            return ResponseEntity.status(403)
                    .body(ApiResponseGenerator.fail(ErrorCode.COMMENT_FORBIDDEN, (Void) null));
        } catch (RuntimeException e) {
            log.error("❌ [CommentController] 댓글 삭제 실패: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.COMMENT_DELETE_FAILED, (Void) null));
        } catch (Exception e) {
            log.error("❌ [CommentController] 예상치 못한 오류: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        log.info("📝 [CommentController] 댓글 수정: userId={}, commentId={}", userNo, commentId);
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [CommentController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }
        
        // 파라미터 유효성 검증
        if (commentId == null || commentId <= 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 댓글 ID: {}", commentId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_COMMENT_ID, (Void) null));
        }
        
        if (userNo <= 0) {
            log.warn("⚠️ [CommentController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        }
        
        // 요청 데이터 유효성 검증
        if (request == null) {
            log.warn("⚠️ [CommentController] 요청 데이터가 null입니다");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            log.warn("⚠️ [CommentController] 댓글 내용이 비어있습니다");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_CONTENT, (Void) null));
        }
        
        try {
            commentService.updateComment(userNo, commentId, request);
            log.info("✅ [CommentController] 댓글 수정 성공: userId={}, commentId={}", userNo, commentId);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (IllegalArgumentException e) {
            log.error("❌ [CommentController] 잘못된 파라미터: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        } catch (AccessDeniedException e) {
            log.error("❌ [CommentController] 접근 권한 없음: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage());
            return ResponseEntity.status(403)
                    .body(ApiResponseGenerator.fail(ErrorCode.COMMENT_FORBIDDEN, (Void) null));
        } catch (RuntimeException e) {
            log.error("❌ [CommentController] 댓글 수정 실패: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        } catch (Exception e) {
            log.error("❌ [CommentController] 예상치 못한 오류: userId={}, commentId={}, error={}", userNo, commentId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }
}
