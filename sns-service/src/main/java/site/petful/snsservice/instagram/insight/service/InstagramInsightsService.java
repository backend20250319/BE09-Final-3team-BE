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

    public void syncInsightRecentSixMonth(Long instagramId, Long userId) {
        String metric = "shares,likes,comments,views,reach";
        String accessToken = instagramTokenService.getAccessTokenByUserId(userId);
        InstagramProfileEntity profileEntity = instagramProfileRepository.findById(
            instagramId).orElseThrow(() ->
            new IllegalArgumentException("인스타 프로필을 찾을 수 없습니다.: " + instagramId));

        List<InstagramInsightEntity> entities = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        // 최근 12개월을 반달씩 나누어 처리 (총 24개 요청)
        for (int i = 0; i < 12; i++) {
            LocalDate targetMonth = currentDate.minusMonths(i);
            // 각 월의 두 번째 반 (16일~월말) - 최신부터 처리
            long secondHalfSince = DateTimeUtils.getSecondHalfOfMonthStart(targetMonth);
            long secondHalfUntil = DateTimeUtils.getSecondHalfOfMonthEnd(targetMonth);

            System.out.printf("두 번째 반 - since: %d, until: %d (%s 16일~말일)%n",
                secondHalfSince, secondHalfUntil, targetMonth.toString().substring(0, 7));

            InstagramInsightsResponseDto secondHalfResponse = instagramApiClient.fetchInstagramInsights(
                instagramId,
                accessToken,
                secondHalfSince,
                secondHalfUntil,
                metric
            );

            entities.add(new InstagramInsightEntity(secondHalfResponse, profileEntity,
                DateTimeUtils.fromUnixTimeToLocalDateTime(secondHalfSince),
                DateTimeUtils.fromUnixTimeToLocalDateTime(secondHalfUntil)));

            // 각 월의 첫 번째 반 (1일~15일)
            long firstHalfSince = DateTimeUtils.getFirstHalfOfMonthStart(targetMonth);
            long firstHalfUntil = DateTimeUtils.getFirstHalfOfMonthEnd(targetMonth);

            System.out.printf("첫 번째 반 - since: %d, until: %d (%s 1일~15일)%n",
                firstHalfSince, firstHalfUntil, targetMonth.toString().substring(0, 7));

            InstagramInsightsResponseDto firstHalfResponse = instagramApiClient.fetchInstagramInsights(
                instagramId,
                accessToken,
                firstHalfSince,
                firstHalfUntil,
                metric
            );

            entities.add(new InstagramInsightEntity(firstHalfResponse, profileEntity,
                DateTimeUtils.fromUnixTimeToLocalDateTime(firstHalfSince),
                DateTimeUtils.fromUnixTimeToLocalDateTime(firstHalfUntil)));
        }

        entities = instagramInsightRepository.saveAll(entities);
    }
}
