package site.petful.snsservice.instagram.client;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponse;


@Component // Spring Bean으로 등록
public class InstagramApiClient {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;

    // API 통신에 필요한 의존성만 주입받습니다.
    public InstagramApiClient(
        WebClient webClient,
        @Value("${instagram.api.client_id}") String clientId,
        @Value("${instagram.api.client_secret}") String clientSecret) {
        this.webClient = webClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Mono<InstagramTokenResponse> getLongLivedAccessToken(String shortLivedToken) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/oauth/access_token")
                .queryParam("grant_type", "fb_exchange_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("fb_exchange_token", shortLivedToken)
                .build())
            .retrieve()
            .bodyToMono(InstagramTokenResponse.class);
    }

    public Mono<List<String>> fetchInstagramIds(String userAccessToken) {
        Mono<String> response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/me/accounts")
                .queryParam("access_token", userAccessToken)
                .queryParam("fields", "name,access_token,instagram_business_account")
                .build())
            .retrieve()
            .bodyToMono(String.class);

        return response.map(jsonString -> {
            // 3. JsonPath 표현식으로 원하는 ID 목록만 정확히 추출합니다.
            String jsonPathExpression = "$.data[*].instagram_business_account.id";
            return JsonPath.read(jsonString, jsonPathExpression);
        });
    }
}