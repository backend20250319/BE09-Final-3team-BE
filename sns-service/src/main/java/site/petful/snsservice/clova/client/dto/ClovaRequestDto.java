package site.petful.snsservice.clova.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClovaRequestDto {

    private List<Message> messages;
    private double topP;
    private int topK;
    private int maxTokens;
    private double temperature;
    @JsonProperty("repetition_penalty") // JSON 필드명과 다를 경우 매핑
    private double repetitionPenalty;
    private List<String> stop;
    private int seed;
    private boolean includeAiFilters;


    public static ClovaRequestDto defaultRequest(List<Message> messages) {
        return ClovaRequestDto.builder()
            .messages(messages)
            .topP(0.6)
            .topK(0)
            .maxTokens(256)
            .temperature(0.1)
            .repetitionPenalty(1.1)
            .stop(List.of("###"))
            .seed(0)
            .includeAiFilters(true)
            .build();
    }

    @AllArgsConstructor
    @Getter
    public static class Message {

        private String role;
        private List<ContentPart> content;

        // 생성자, Getter, Setter
    }

    @AllArgsConstructor
    @Getter
    public static class ContentPart {

        private String type;
        private String text;
    }
}

