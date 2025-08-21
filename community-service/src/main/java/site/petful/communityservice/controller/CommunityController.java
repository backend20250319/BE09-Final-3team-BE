package site.petful.communityservice.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.common.ErrorCode;
import site.petful.communityservice.dto.PostCreateRequest;
import site.petful.communityservice.dto.PostDto;
import site.petful.communityservice.service.CommunityService;

@RestController
@RequestMapping("community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/register")
    public ApiResponse<Void> newRegistration(
            @RequestHeader(value = "X-User-No",   required = false) Long userNo,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody PostCreateRequest request
    ) {
        if(userNo == null){
            return ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST,"해당 유저가 존재하지 않습니다.");
        }
        PostDto dto = communityService.registNewPost(userNo, request);
        return ApiResponseGenerator.success();
    }

}
