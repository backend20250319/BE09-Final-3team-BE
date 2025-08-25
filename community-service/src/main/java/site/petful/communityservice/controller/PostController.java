package site.petful.communityservice.controller;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.common.ErrorCode;
import site.petful.communityservice.common.PageResponse;
import site.petful.communityservice.dto.PostItem;
import site.petful.communityservice.dto.PostCreateRequest;

import site.petful.communityservice.dto.PostDetail;
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
            @RequestHeader(value = "X-User-No",required = false) Long userNo,
            @RequestHeader(value = "X-User-Type",required = false) String userType,
            @RequestBody PostCreateRequest request
    ) {
        if(userNo == null){
            return ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST,"해당 유저가 존재하지 않습니다.");
        }
        postService.registNewPost(userNo, request);
        return ApiResponseGenerator.success();
    }
    //전체 게시글 조회
    @GetMapping()
    public ApiResponse<PageResponse<PostItem>> getPosts(
            @RequestHeader(value = "X-User-No",required = false) Long userNo,
            @RequestHeader(value = "X-User-Type",required = false) String userType,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "20")int size,
            @PageableDefault() Pageable pageable,
            @RequestParam(required = false)PostType type
            ){
           Page<PostItem> result = postService.getPosts(page,size,type);
           return ApiResponseGenerator.success(PageResponse.of(result));
    }
    //전체 게시글 조회
    @GetMapping("/me")
    public ApiResponse<PageResponse<PostItem>> getMyPosts(
            @RequestHeader(value = "X-User-No",required = false) Long userNo,
            @RequestHeader(value = "X-User-Type",required = false) String userType,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "20")int size,
            @RequestParam(required = false)PostType type
    ){
        Page<PostItem> result = postService.getMyPosts(userNo,page,size,type);
        return ApiResponseGenerator.success(PageResponse.of(result));
    }
    //게시글 상세보기
    @GetMapping("/{id}/detail")
    public ApiResponse<PostDetail> postDetail(
            @RequestHeader(value = "X-User-No",required = false) Long userNo,
            @RequestHeader(value = "X-User-Type",required = false) String userType,
            @PathVariable Long postId
    ){
        return ApiResponseGenerator.success(postService.getPostDetail(userNo,postId));
    }
    //게시글 삭제
    @DeleteMapping("/{id}/delete")
    public ApiResponse<Void> deletePost(
            @RequestHeader(value = "X-User-No",required = false) Long userNo,
            @RequestHeader(value = "X-User-Type",required = false) String userType,
            @PathVariable Long postId
    ) throws AccessDeniedException {
        postService.deletePost(userNo,postId);
        return ApiResponseGenerator.success();
    }
}
