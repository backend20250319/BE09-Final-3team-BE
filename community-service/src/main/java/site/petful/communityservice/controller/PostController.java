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
    public ApiResponse<Void> newRegistration(
            @AuthenticationPrincipal Long userNo,
            @RequestBody PostCreateRequest request
    ) {
//        if (userNo == null) {
//            return ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST, "인증 정보가 없습니다.");
//        }
        postService.registNewPost(userNo, request);
        return ApiResponseGenerator.success();
    }
    //전체 게시글 조회
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PageResponse<PostItem>>> getPosts(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam(required = false)PostType type
            ){
        log.info("getPosts called - page={}, size={}, type={}", pageable.getPageNumber(), pageable.getPageSize(), type);
        try {
            Page<PostItem> result = postService.getPosts(pageable,type);
            log.info("getPosts completed - total elements: {}, total pages: {}", result.getTotalElements(), result.getTotalPages());
            
            // 첫 번째 항목의 author 정보 로깅 (디버깅용)
            if (!result.getContent().isEmpty()) {
                PostItem firstItem = result.getContent().get(0);
                log.info("First post author info - id: {}, nickname: {}, profileImageUrl: {}", 
                        firstItem.getAuthor() != null ? firstItem.getAuthor().getId() : "null",
                        firstItem.getAuthor() != null ? firstItem.getAuthor().getNickname() : "null",
                        firstItem.getAuthor() != null ? firstItem.getAuthor().getProfileImageUrl() : "null");
            }
            
            return ResponseEntity.ok(ApiResponseGenerator.success(PageResponse.of(result)));
        } catch (Exception e) {
            log.error("Error in getPosts: ", e);
            throw e;
        }
    }
    // 게시글 조회
    @GetMapping("/me")
    public ApiResponse<PageResponse<PostItem>> getMyPosts(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable,
            @RequestParam(required = false)PostType type
    ){
        Page<PostItem> result = postService.getMyPosts(userNo,pageable,type);
        return ApiResponseGenerator.success(PageResponse.of(result));
    }
    //게시글 상세보기
    @GetMapping("/{postId}/detail")
    public ApiResponse<PostDetailDto> postDetail(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PathVariable Long postId
    ){
        return ApiResponseGenerator.success(postService.getPostDetail(userNo,postId));
    }
    //게시글 삭제
    @PatchMapping("/{postId}/delete")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PathVariable Long postId
    ) throws AccessDeniedException {
        postService.deletePost(userNo,postId);
        return ApiResponseGenerator.success();
    }
}
