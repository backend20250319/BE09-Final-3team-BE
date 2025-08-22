package site.petful.snsservice.instagram.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.snsservice.instagram.client.dto.InstagramCommentResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramMediaResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramTokenResponseDto;


@FeignClient(name = "instagramApiClient", url = "https://graph.facebook.com/v23.0")
public interface InstagramApiClient {

    @GetMapping("/oauth/access_token?grant_type=fb_exchange_token")
    InstagramTokenResponseDto getLongLivedAccessToken(
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("fb_exchange_token") String shortLivedToken
    );

    @GetMapping("/me/accounts?fields=instagram_business_account")
    String fetchInstagramAccounts(
        @RequestParam("access_token") String accessToken
    );

    @GetMapping("/{instagramId}")
    InstagramProfileResponseDto fetchInstagramProfile(
        @PathVariable("instagramId") Long instagramId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("fields") String fields
    );

    @GetMapping("/{instagramId}/media")
    InstagramMediaResponseDto fetchInstagramMedia(
        @PathVariable("instagramId") Long instagramId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("fields") String fields,
        @RequestParam("after") String after,
        @RequestParam("limit") int limit
    );

    @GetMapping("/{media_id}/comments")
    InstagramCommentResponseDto fetchInstagramComments(
        @PathVariable("media_id") Long mediaId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("fields") String fields,
        @RequestParam("after") String after,
        @RequestParam("limit") int limit
    );


}