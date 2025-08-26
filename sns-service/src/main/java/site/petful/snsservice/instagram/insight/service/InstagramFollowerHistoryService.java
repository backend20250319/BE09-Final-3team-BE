package site.petful.snsservice.instagram.insight.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.snsservice.instagram.insight.dto.InstagramFollowerHistoryResponseDto;
import site.petful.snsservice.instagram.insight.entity.InstagramFollowerHistoryEntity;
import site.petful.snsservice.instagram.insight.repository.InstagramFollowerHistoryRepository;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;
import site.petful.snsservice.instagram.profile.repository.InstagramProfileRepository;
import site.petful.snsservice.util.DateTimeUtils;

@Service
@RequiredArgsConstructor
public class InstagramFollowerHistoryService {

    private final InstagramFollowerHistoryRepository instagramFollowerHistoryRepository;
    private final InstagramProfileRepository instagramProfileRepository;

    public void saveFollowerHistory(Long instagramId, LocalDate date, long followerCount) {
        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() -> new IllegalArgumentException("해당 인스타그램 아이디가 없습니다. " + instagramId));

        InstagramFollowerHistoryEntity entity = new InstagramFollowerHistoryEntity(
            null, profile, date, followerCount);

        instagramFollowerHistoryRepository.save(entity);
    }

    public List<InstagramFollowerHistoryResponseDto> findAllByInstagramIdRecently6Month(
        Long instagramId) {

        InstagramProfileEntity profile = instagramProfileRepository.findById(instagramId)
            .orElseThrow(() ->
                new IllegalArgumentException("해당 인스타그램 아이디가 없습니다. " + instagramId));
        
        List<InstagramFollowerHistoryEntity> entities = instagramFollowerHistoryRepository.findByInstagramProfileAndMonthGreaterThanEqual(
            profile, DateTimeUtils.getStartOfCurrentMonth().minusMonths(6).toLocalDate());

        return entities.stream()
            .map(entity -> new InstagramFollowerHistoryResponseDto(
                entity.getInstagramProfile().getId(),
                entity.getMonth().toString(),
                entity.getTotalFollowers()
            ))
            .toList();
    }
}
