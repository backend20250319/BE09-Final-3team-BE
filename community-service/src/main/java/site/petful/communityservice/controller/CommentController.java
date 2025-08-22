package site.petful.communityservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.dto.CommentCreateRequest;
import site.petful.communityservice.dto.CommentCreateResponse;
import site.petful.communityservice.service.CommentService;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/community/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ApiResponse<CommentCreateResponse> create(
            @RequestHeader("X-User-No") Long userNo,
            @RequestBody CommentCreateRequest request
    ) {
       CommentCreateResponse response = commentService.createComment(userNo,request);
       return ApiResponseGenerator.success(response);
    }

    @DeleteMapping("/{id}/delete")
    public ApiResponse<Boolean> delete(
            @RequestHeader("X-User-No") Long userNo,
            @RequestHeader("X-User-Role")String role,
            @PathVariable Long commentId
    ) throws AccessDeniedException {
        return ApiResponseGenerator.success(commentService.deleteComment(userNo,commentId,role));
    }
}
