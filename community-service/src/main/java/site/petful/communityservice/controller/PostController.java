package site.petful.communityservice.controller;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.common.ErrorCode;
import site.petful.communityservice.common.PageResponse;
import site.petful.communityservice.dto.PostItem;
import site.petful.communityservice.dto.PostCreateRequest;

import site.petful.communityservice.dto.PostDetailDto;
import site.petful.communityservice.entity.PostType;
import site.petful.communityservice.service.PostService;

import java.nio.file.AccessDeniedException;


@Slf4j
@RestController
@RequestMapping("community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    //게시글 등록
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> newRegistration(
            @AuthenticationPrincipal Long userNo,
            @RequestBody PostCreateRequest request
    ) {
        log.info("📝 [PostController] 게시글 등록 요청: userId={}", userNo);
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [PostController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }
        
        // 사용자 ID 유효성 검증
        if (userNo <= 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        }
        
        // 요청 데이터 유효성 검증
        if (request == null) {
            log.warn("⚠️ [PostController] 요청 데이터가 null입니다");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        }
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.warn("⚠️ [PostController] 제목이 비어있습니다");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_CONTENT, (Void) null));
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            log.warn("⚠️ [PostController] 내용이 비어있습니다");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_CONTENT, (Void) null));
        }
        
        try {
            postService.registNewPost(userNo, request);
            log.info("✅ [PostController] 게시글 등록 성공: userId={}", userNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (IllegalArgumentException e) {
            log.error("❌ [PostController] 잘못된 파라미터: userId={}, error={}", userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        } catch (RuntimeException e) {
            log.error("❌ [PostController] 게시글 등록 실패: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.POST_CREATE_FAILED, (Void) null));
        } catch (Exception e) {
            log.error("❌ [PostController] 예상치 못한 오류: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }
    //전체 게시글 조회
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<PostItem>>> getPosts(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam(required = false)PostType type
            ){
        log.info("📋 [PostController] 전체 게시글 조회: page={}, size={}, type={}", 
                pageable.getPageNumber(), pageable.getPageSize(), type);
        
        // 페이지 파라미터 유효성 검증
        if (pageable.getPageNumber() < 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 페이지 번호: {}", pageable.getPageNumber());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (PageResponse<PostItem>) null));
        }
        
        if (pageable.getPageSize() <= 0 || pageable.getPageSize() > 100) {
            log.warn("⚠️ [PostController] 유효하지 않은 페이지 크기: {}", pageable.getPageSize());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (PageResponse<PostItem>) null));
        }
        
        try {
            Page<PostItem> result = postService.getPosts(pageable, type);
            log.info("✅ [PostController] 전체 게시글 조회 성공: totalElements={}, totalPages={}", 
                    result.getTotalElements(), result.getTotalPages());
            
            // 첫 번째 항목의 author 정보 로깅 (디버깅용)
            if (!result.getContent().isEmpty()) {
                PostItem firstItem = result.getContent().get(0);
                log.info("🔍 [PostController] 첫 번째 게시글 작성자 정보 - id: {}, nickname: {}, profileImageUrl: {}", 
                        firstItem.getAuthor() != null ? firstItem.getAuthor().getId() : "null",
                        firstItem.getAuthor() != null ? firstItem.getAuthor().getNickname() : "null",
                        firstItem.getAuthor() != null ? firstItem.getAuthor().getProfileImageUrl() : "null");
            }
            
            return ResponseEntity.ok(ApiResponseGenerator.success(PageResponse.of(result)));
        } catch (IllegalArgumentException e) {
            log.error("❌ [PostController] 잘못된 파라미터: error={}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (PageResponse<PostItem>) null));
        } catch (RuntimeException e) {
            log.error("❌ [PostController] 게시글 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.OPERATION_FAILED, (PageResponse<PostItem>) null));
        } catch (Exception e) {
            log.error("❌ [PostController] 예상치 못한 오류: error={}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (PageResponse<PostItem>) null));
        }
    }
    // 게시글 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<PostItem>>> getMyPosts(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam(required = false)PostType type
    ){
        log.info("📋 [PostController] 내 게시글 조회: userId={}, page={}, size={}, type={}", 
                userNo, pageable.getPageNumber(), pageable.getPageSize(), type);
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [PostController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (PageResponse<PostItem>) null));
        }
        
        // 사용자 ID 유효성 검증
        if (userNo <= 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (PageResponse<PostItem>) null));
        }
        
        // 페이지 파라미터 유효성 검증
        if (pageable.getPageNumber() < 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 페이지 번호: {}", pageable.getPageNumber());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (PageResponse<PostItem>) null));
        }
        
        if (pageable.getPageSize() <= 0 || pageable.getPageSize() > 100) {
            log.warn("⚠️ [PostController] 유효하지 않은 페이지 크기: {}", pageable.getPageSize());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (PageResponse<PostItem>) null));
        }
        
        try {
            Page<PostItem> result = postService.getMyPosts(userNo, pageable, type);
            log.info("✅ [PostController] 내 게시글 조회 성공: userId={}, totalElements={}", 
                    userNo, result.getTotalElements());
            return ResponseEntity.ok(ApiResponseGenerator.success(PageResponse.of(result)));
        } catch (IllegalArgumentException e) {
            log.error("❌ [PostController] 잘못된 파라미터: userId={}, error={}", userNo, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (PageResponse<PostItem>) null));
        } catch (RuntimeException e) {
            log.error("❌ [PostController] 내 게시글 조회 실패: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.OPERATION_FAILED, (PageResponse<PostItem>) null));
        } catch (Exception e) {
            log.error("❌ [PostController] 예상치 못한 오류: userId={}, error={}", userNo, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode. SYSTEM_ERROR, (PageResponse<PostItem>) null));
        }
    }
    //게시글 상세보기
    @GetMapping("/{postId}/detail")
    public ResponseEntity<ApiResponse<PostDetailDto>> postDetail(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PathVariable Long postId
    ){
        log.info("📋 [PostController] 게시글 상세보기: userId={}, postId={}", userNo, postId);
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [PostController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (PostDetailDto) null));
        }
        
        // 파라미터 유효성 검증
        if (postId == null || postId <= 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 게시글 ID: {}", postId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_POST_ID, (PostDetailDto) null));
        }
        
        if (userNo <= 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (PostDetailDto) null));
        }
        
        try {
            PostDetailDto result = postService.getPostDetail(userNo, postId);
            log.info("✅ [PostController] 게시글 상세보기 성공: userId={}, postId={}", userNo, postId);
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (IllegalArgumentException e) {
            log.error("❌ [PostController] 잘못된 파라미터: userId={}, postId={}, error={}", userNo, postId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (PostDetailDto) null));
        } catch (RuntimeException e) {
            log.error("❌ [PostController] 게시글 상세보기 실패: userId={}, postId={}, error={}", userNo, postId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.POST_NOT_FOUND, (PostDetailDto) null));
        } catch (Exception e) {
            log.error("❌ [PostController] 예상치 못한 오류: userId={}, postId={}, error={}", userNo, postId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (PostDetailDto) null));
        }
    }
    //게시글 삭제
    @PatchMapping("/{postId}/delete")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PathVariable Long postId
    ) {
        log.info("🗑️ [PostController] 게시글 삭제: userId={}, postId={}", userNo, postId);
        
        // 인증 검증
        if (userNo == null) {
            log.warn("⚠️ [PostController] 인증되지 않은 사용자 요청");
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, (Void) null));
        }
        
        // 파라미터 유효성 검증
        if (postId == null || postId <= 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 게시글 ID: {}", postId);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_POST_ID, (Void) null));
        }
        
        if (userNo <= 0) {
            log.warn("⚠️ [PostController] 유효하지 않은 사용자 ID: {}", userNo);
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_USER_ID, (Void) null));
        }
        
        try {
            postService.deletePost(userNo, postId);
            log.info("✅ [PostController] 게시글 삭제 성공: userId={}, postId={}", userNo, postId);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (IllegalArgumentException e) {
            log.error("❌ [PostController] 잘못된 파라미터: userId={}, postId={}, error={}", userNo, postId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, (Void) null));
        } catch (AccessDeniedException e) {
            log.error("❌ [PostController] 접근 권한 없음: userId={}, postId={}, error={}", userNo, postId, e.getMessage());
            return ResponseEntity.status(403)
                    .body(ApiResponseGenerator.fail(ErrorCode.POST_FORBIDDEN, (Void) null));
        } catch (RuntimeException e) {
            log.error("❌ [PostController] 게시글 삭제 실패: userId={}, postId={}, error={}", userNo, postId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.POST_DELETE_FAILED, (Void) null));
        } catch (Exception e) {
            log.error("❌ [PostController] 예상치 못한 오류: userId={}, postId={}, error={}", userNo, postId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR, (Void) null));
        }
    }
}
