package site.petful.snsservice.instagram.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponse;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponse;


@FeignClient(name = "instagramApiClient", url = "https://graph.facebook.com/v23.0")
public interface InstagramApiClient {

    @GetMapping("/oauth/access_token?grant_type=fb_exchange_token")
    InstagramTokenResponse getLongLivedAccessToken(
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("fb_exchange_token") String shortLivedToken
    );

    @GetMapping("/me/accounts?fields=instagram_business_account")
    String fetchInstagramAccounts(
        @RequestParam("access_token") String accessToken
    );

    @GetMapping("/{instagramId}")
    InstagramProfileResponse fetchInstagramProfile(
        @PathVariable("instagramId") Long instagramId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("fields") String fields
    );


}