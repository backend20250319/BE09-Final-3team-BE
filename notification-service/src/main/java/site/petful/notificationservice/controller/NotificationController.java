package site.petful.notificationservice.controller;




import org.springframework.data.domain.Slice;
import site.petful.notificationservice.common.ApiResponse;
import site.petful.notificationservice.common.ApiResponseGenerator;
import site.petful.notificationservice.common.ErrorCode;
import site.petful.notificationservice.dto.NotificationDto;
import site.petful.notificationservice.dto.SliceResponseDto;
import site.petful.notificationservice.service.NotificationReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationReadService notificationService;



    @GetMapping
    public ApiResponse<SliceResponseDto<NotificationDto>> list(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "30") Integer size
    ) {
        Slice slice = notificationService.listVisible(userId, page, size);
        SliceResponseDto<NotificationDto> payload = new SliceResponseDto<>(
                slice.getContent(), slice.getNumber(), slice.getSize(), slice.hasNext()
        );
        return ApiResponseGenerator.success(payload);
    }

    @PatchMapping("{id}/hide")
    public ApiResponse<Void> hide(
            @PathVariable Long id,
            @RequestParam Long userId
    ){
        notificationService.hide(id, userId);
        return ApiResponseGenerator.success();
    }
}
