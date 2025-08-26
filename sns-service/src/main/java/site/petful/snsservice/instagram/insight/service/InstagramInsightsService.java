package site.petful.snsservice.instagram.insight.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramApiInsightsResponseDto;
import site.petful.snsservice.instagram.insight.dto.InstagramEngagementResponseDto;
import site.petful.snsservice.instagram.insight.dto.InstagramInsightResponseDto;
import site.petful.snsservice.instagram.insight.entity.InstagramInsightEntity;
import site.petful.snsservice.instagram.insight.repository.InstagramInsightRepository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;
import site.petful.snsservice.instagram.profile.repository.InstagramProfileRepository;
import site.petful.snsservice.util.DateTimeUtils;

@Service
@RequiredArgsConstructor
public class InstagramInsightsService {

    private final InstagramApiClient instagramApiClient;
    private final InstagramTokenService instagramTokenService;
    private final InstagramProfileRepository instagramProfileRepository;
    private final InstagramInsightRepository instagramInsightRepository;

    private static final String INSIGHT_METRICS = "shares,likes,comments,views,reach";

    public void syncInsightRecentOneMonth(Long instagramId, Long userId) {
        syncInsights(instagramId, userId, 1);
    }


    public void syncInsightRecentSixMonth(Long instagramId, Long userId) {
        syncInsights(instagramId, userId, 6);
    }


    private void syncInsights(Long instagramId, Long userId, int monthsToSync) {
        String accessToken = instagramTokenService.getAccessToken(userId);
        InstagramProfileEntity profileEntity = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new IllegalArgumentException("인스타 프로필을 찾을 수 없습니다.: " + instagramId));

        List<InstagramInsightEntity> entitiesToSave = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < monthsToSync; i++) {
            LocalDate targetMonth = currentDate.minusMonths(i);
            entitiesToSave.add(
                fetchInsightsForMonth(instagramId, accessToken, profileEntity, targetMonth));
        }

        instagramInsightRepository.saveAll(entitiesToSave);
    }

    private InstagramInsightEntity fetchInsightsForMonth(Long instagramId, String accessToken,
        InstagramProfileEntity profileEntity, LocalDate targetMonth) {
        String monthString = targetMonth.toString().substring(0, 7);

        long firstHalfSince = DateTimeUtils.getFirstHalfOfMonthStart(targetMonth);
        long firstHalfUntil = DateTimeUtils.getFirstHalfOfMonthEnd(targetMonth);
        System.out.printf("첫 번째 반 - since: %d, until: %d (%s 1일~15일)%n", firstHalfSince,
            firstHalfUntil, monthString);
        InstagramApiInsightsResponseDto firstHalf = fetchInsightForPeriod(instagramId, accessToken,
            firstHalfSince, firstHalfUntil);

        long secondHalfSince = DateTimeUtils.getSecondHalfOfMonthStart(targetMonth);
        long secondHalfUntil = DateTimeUtils.getSecondHalfOfMonthEnd(targetMonth);
        System.out.printf("두 번째 반 - since: %d, until: %d (%s 16일~말일)%n", secondHalfSince,
            secondHalfUntil, monthString);
        InstagramApiInsightsResponseDto secondHalf = fetchInsightForPeriod(instagramId, accessToken,
            secondHalfSince, secondHalfUntil);

        // 👉 두 엔티티를 하나로 합치기
        return mergeInsights(firstHalf, secondHalf, profileEntity, firstHalfSince, secondHalfUntil);
    }

    private InstagramInsightEntity mergeInsights(InstagramApiInsightsResponseDto firstHalf,
        InstagramApiInsightsResponseDto secondHalf, InstagramProfileEntity profile, long since,
        long until) {

        long shares = 0L;
        long likes = 0L;
        long comments = 0L;
        long views = 0L;
        long reach = 0L;

        shares += extractValue(firstHalf, "shares") + extractValue(secondHalf, "shares");
        likes += extractValue(firstHalf, "likes") + extractValue(secondHalf, "likes");
        comments += extractValue(firstHalf, "comments") + extractValue(secondHalf, "comments");
        views += extractValue(firstHalf, "views") + extractValue(secondHalf, "views");
        reach += extractValue(firstHalf, "reach") + extractValue(secondHalf, "reach");

        return new InstagramInsightEntity(
            null,
            profile,
            shares,
            likes,
            comments,
            views,
            reach,
            DateTimeUtils.fromUnixTimeToLocalDateTime(since).toLocalDate(),
            DateTimeUtils.fromUnixTimeToLocalDateTime(until).toLocalDate()
        );
    }

    private long extractValue(InstagramApiInsightsResponseDto dto, String metric) {
        return dto.getData().stream()
            .filter(data -> data.getName().equals(metric))
            .map(d -> d.getTotalValue().getValue())
            .findFirst()
            .orElse(0L);
    }


    private InstagramApiInsightsResponseDto fetchInsightForPeriod(Long instagramId,
        String accessToken,
        long since, long until) {
        return instagramApiClient.fetchInsights(
            instagramId,
            accessToken,
            since,
            until,
            INSIGHT_METRICS
        );

    }

    public List<InstagramInsightResponseDto> getInsightGraphData(Long instagramId) {
        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new IllegalArgumentException("인스타 프로필을 찾을 수 없습니다.: " + instagramId));

        LocalDate sixMonthsAgo = DateTimeUtils.getStartOfCurrentMonth().minusMonths(7)
            .toLocalDate();

        List<InstagramInsightEntity> insights = instagramInsightRepository.findByInstagramProfileAndSinceAfter(
            profile, sixMonthsAgo);

        return insights.stream()
            .map(InstagramInsightResponseDto::fromEntity)
            .toList();
    }

    public InstagramEngagementResponseDto getEngagementData(Long instagramId) {
        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new IllegalArgumentException("인스타 프로필을 찾을 수 없습니다.: " + instagramId));

        List<InstagramInsightEntity> insights = instagramInsightRepository.findByInstagramProfile(
            profile);

        if (insights.isEmpty()) {
            return new InstagramEngagementResponseDto(0.0, 0.0, 0.0);
        }

        long totalLikes = 0;
        long totalComments = 0;
        long totalShares = 0;

        for (InstagramInsightEntity insight : insights) {
            totalLikes += insight.getLikes();
            totalComments += insight.getComments();
            totalShares += insight.getShares();
        }

        double totalEngagements = totalLikes + totalComments + totalShares;
        return new InstagramEngagementResponseDto(
            totalLikes / totalEngagements * 100,
            totalComments / totalEngagements * 100,
            totalShares / totalEngagements * 100
        );
    }
}