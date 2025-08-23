package site.petful.snsservice.clova.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.petful.snsservice.clova.client.config.ClovaFeignConfiguration;
import site.petful.snsservice.clova.client.dto.ClovaRequestDto;
import site.petful.snsservice.clova.client.dto.ClovaResponseDto;

@FeignClient(
    name = "clovaApiClient",
    url = "https://clovastudio.stream.ntruss.com",
    configuration = ClovaFeignConfiguration.class
)
public interface ClovaApiClient {

    @PostMapping("/v3/chat-completions/HCX-005")
    ClovaResponseDto getSentiment(@RequestBody ClovaRequestDto request);
}
