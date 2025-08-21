package site.petful.communityservice.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.common.ErrorCode;
import site.petful.communityservice.dto.PostCreateRequest;
import site.petful.communityservice.dto.PostDto;
import site.petful.communityservice.service.CommunityService;

import java.util.Map;

@RestController
@RequestMapping("community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/register")
    public ApiResponse<Void> newRegistration(
            @RequestHeader(value = "X-User-N0",   required = false) Long userNo,
            @RequestHeader(value = "X-User-Type", required = false) String userType,
            @RequestBody PostCreateRequest request
    ) {
        if(userNo == null){
            return ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST,"해당 유저가 존재하지 않습니다.");
        }
        communityService.registNewPost(userNo, request);
        return ApiResponseGenerator.success();
    }
    @GetMapping("/debug-headers")
    public Map<String, String> debugHeaders(@RequestHeader HttpHeaders headers) {
        return Map.of(
                "HDR_USER_NO", headers.getFirst("HDR_USER_NO"),
                "HDR_USER_TYPE", headers.getFirst("HDR_USER_TYPE")
        );
    }

}
