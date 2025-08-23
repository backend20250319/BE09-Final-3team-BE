package site.petful.snsservice.clova.client.dto;

import lombok.Getter;

@Getter
public class ClovaResponseDto {

    private Result result;

    @Getter
    public static class Result {

        private ResponseMessage message;

        // Getter
        public ResponseMessage getMessage() {
            return message;
        }
    }

    @Getter
    public static class ResponseMessage {

        private String role;
        private String content; // 우리가 원하는 최종 값

        // Getters
        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
