package site.petful.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

/**
 * 웹푸시 구독 요청을 위한 DTO
 * 
 * 프론트엔드에서 브라우저의 구독 정보를 서버로 전송할 때 사용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebPushSubscriptionRequest {

    /**
     * 브라우저에서 생성된 구독 엔드포인트
     * 예: https://fcm.googleapis.com/fcm/send/...
     */
    @NotBlank(message = "엔드포인트는 필수입니다")
    private String endpoint;

    /**
     * P-256 공개키 (Base64 인코딩)
     * 클라이언트에서 생성한 공개키
     */
    @NotBlank(message = "P256DH 키는 필수입니다")
    private String p256dhKey;

    /**
     * 인증 키 (Base64 인코딩)
     * 클라이언트에서 생성한 인증 키
     */
    @NotBlank(message = "Auth 키는 필수입니다")
    private String authKey;
}
