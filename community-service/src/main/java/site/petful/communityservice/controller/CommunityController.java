package site.petful.communityservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import site.petful.communityservice.common.ApiResponse;
import site.petful.communityservice.common.ApiResponseGenerator;
import site.petful.communityservice.common.ErrorCode;
//import site.petful.communityservice.common.PageResponse;
import site.petful.communityservice.common.PageResponse;
import site.petful.communityservice.dto.MyPostItem;
import site.petful.communityservice.dto.PostCreateRequest;

import site.petful.communityservice.entity.PostType;
import site.petful.communityservice.service.CommunityService;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("community/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/register")
    public ApiResponse<Void> newRegistration(
            @RequestHeader(value = "X-User-No",required = false) Long userNo,
            @RequestHeader(value = "X-User-Type",required = false) String userType,
            @RequestBody PostCreateRequest request
    ) {
        if(userNo == null){
            return ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST,"해당 유저가 존재하지 않습니다.");
        }
        communityService.registNewPost(userNo, request);
        return ApiResponseGenerator.success();
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<MyPostItem>> myposts(
            @RequestHeader(value = "X-User-No",required = false) Long userNo,
            @RequestHeader(value = "X-User-Type",required = false) String userType,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "20")int size,
            @RequestParam(required = false)PostType type
            ){
           Page<MyPostItem> result = communityService.getMyPosts(userNo,page,size,type);
           return ApiResponseGenerator.success(PageResponse.of(result));
    }

}
