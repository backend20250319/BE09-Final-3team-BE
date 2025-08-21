package site.petful.communityservice.controller;


import lombok.RequiredArgsConstructor;

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

    @PostMapping("{id}/register")
    public ApiResponse<Void> newRegistration(
            @RequestParam Long userId,
            @RequestBody PostCreateRequest request
        ){
        if(userId == null){
            return ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST,"해당 유저가 존재하지 않습니다.");
        }
        PostDto dto = communityService.registNewPost(userId, request);
        return ApiResponseGenerator.success(); // code=2000, message="OK"
    }

}
