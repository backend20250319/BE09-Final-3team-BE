package site.petful.snsservice.instagram.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import site.petful.snsservice.instagram.client.dto.InstagramApiCommentResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramApiInsightsResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramApiMediaResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramApiTokenResponseDto;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;


@FeignClient(name = "instagramApiClient", url = "https://graph.facebook.com/v23.0")
public interface InstagramApiClient {

    @GetMapping("/oauth/access_token?grant_type=fb_exchange_token")
    InstagramApiTokenResponseDto getLongLivedAccessToken(
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("fb_exchange_token") String shortLivedToken
    );

    @GetMapping("/me/accounts?fields=instagram_business_account")
    String fetchAccounts(
        @RequestParam("access_token") String accessToken
    );

    @GetMapping("/{instagramId}")
    InstagramProfileDto fetchProfile(
        @PathVariable("instagramId") Long instagramId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("fields") String fields
    );

    @GetMapping("/{instagramId}/media")
    InstagramApiMediaResponseDto fetchMedia(
        @PathVariable("instagramId") Long instagramId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("fields") String fields,
        @RequestParam("after") String after,
        @RequestParam("limit") int limit,
        @RequestParam("since") Long since
    );

    @GetMapping("/{media_id}/comments")
    InstagramApiCommentResponseDto fetchComments(
        @PathVariable("media_id") Long mediaId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("fields") String fields,
        @RequestParam("after") String after,
        @RequestParam("limit") int limit
    );

    @GetMapping("/{instagramId}/insights?period=day&metric_type=total_value")
    InstagramApiInsightsResponseDto fetchInsights(
        @PathVariable("instagramId") Long instagramId,
        @RequestParam("access_token") String accessToken,
        @RequestParam("since") Long since,
        @RequestParam("until") Long until,
        @RequestParam("metric") String metric
    );

    @DeleteMapping("/{commentId}")
    InstagramApiInsightsResponseDto deleteComment(
        @PathVariable("commentId") Long commentId,
        @RequestParam("access_token") String accessToken
    );


}