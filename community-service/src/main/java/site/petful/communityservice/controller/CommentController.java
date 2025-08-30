package site.petful.communityservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.dto.CommentCreateRequest;
import site.petful.communityservice.dto.CommentPageDto;
import site.petful.communityservice.dto.CommentView;
import site.petful.communityservice.service.CommentService;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequestMapping("community/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ApiResponse<CommentPageDto> getComment(
            @PathVariable Long postId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponseGenerator.success(commentService.listComments(postId,pageable));
    }


    @PostMapping("/insert")
    public ApiResponse<CommentView> create(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @RequestBody CommentCreateRequest request
    ) {
       CommentView response = commentService.createComment(userNo,request);
        log.info("userNo={}", userNo);
       return ApiResponseGenerator.success(response);
    }

    @PatchMapping("/{commentId}/delete")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Long userNo,
            @AuthenticationPrincipal String userType,
            @PathVariable Long commentId
    ) throws AccessDeniedException {
        commentService.deleteComment(userNo,commentId,userType);
        return ApiResponseGenerator.success();
    }
}
