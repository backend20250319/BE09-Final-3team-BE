package site.petful.snsservice.instagram.insight.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.snsservice.instagram.auth.service.InstagramTokenService;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.client.dto.InstagramInsightsResponseDto;
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
        String accessToken = instagramTokenService.getAccessTokenByUserId(userId);
        InstagramProfileEntity profileEntity = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new IllegalArgumentException("인스타 프로필을 찾을 수 없습니다.: " + instagramId));

        List<InstagramInsightEntity> entitiesToSave = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < monthsToSync; i++) {
            LocalDate targetMonth = currentDate.minusMonths(i);
            entitiesToSave.addAll(
                fetchInsightsForMonth(instagramId, accessToken, profileEntity, targetMonth));
        }

        instagramInsightRepository.saveAll(entitiesToSave);
    }

    private List<InstagramInsightEntity> fetchInsightsForMonth(Long instagramId, String accessToken,
        InstagramProfileEntity profileEntity, LocalDate targetMonth) {
        List<InstagramInsightEntity> monthlyInsights = new ArrayList<>();
        String monthString = targetMonth.toString().substring(0, 7);

        long firstHalfSince = DateTimeUtils.getFirstHalfOfMonthStart(targetMonth);
        long firstHalfUntil = DateTimeUtils.getFirstHalfOfMonthEnd(targetMonth);
        System.out.printf("첫 번째 반 - since: %d, until: %d (%s 1일~15일)%n", firstHalfSince,
            firstHalfUntil, monthString);
        monthlyInsights.add(
            fetchInsightForPeriod(instagramId, accessToken, profileEntity, firstHalfSince,
                firstHalfUntil));

        long secondHalfSince = DateTimeUtils.getSecondHalfOfMonthStart(targetMonth);
        long secondHalfUntil = DateTimeUtils.getSecondHalfOfMonthEnd(targetMonth);
        System.out.printf("두 번째 반 - since: %d, until: %d (%s 16일~말일)%n", secondHalfSince,
            secondHalfUntil, monthString);
        monthlyInsights.add(
            fetchInsightForPeriod(instagramId, accessToken, profileEntity, secondHalfSince,
                secondHalfUntil));

        return monthlyInsights;
    }

    private InstagramInsightEntity fetchInsightForPeriod(Long instagramId, String accessToken,
        InstagramProfileEntity profileEntity, long since, long until) {
        InstagramInsightsResponseDto response = instagramApiClient.fetchInstagramInsights(
            instagramId,
            accessToken,
            since,
            until,
            INSIGHT_METRICS
        );
        return new InstagramInsightEntity(response, profileEntity,
            DateTimeUtils.fromUnixTimeToLocalDateTime(since),
            DateTimeUtils.fromUnixTimeToLocalDateTime(until));
    }
}