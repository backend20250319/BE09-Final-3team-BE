package site.petful.snsservice.instagram.insight.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.snsservice.instagram.insight.dto.InstagramFollowerHistoryResponseDto;
import site.petful.snsservice.instagram.insight.entity.InstagramFollowerHistoryEntity;
import site.petful.snsservice.instagram.insight.entity.InstagramFollowerHistoryId;
import site.petful.snsservice.instagram.insight.repository.InstagramFollowerHistoryRepository;
import site.petful.snsservice.util.DateTimeUtils;

@Service
@RequiredArgsConstructor
public class InstagramFollowerHistoryService {

    private final InstagramFollowerHistoryRepository instagramFollowerHistoryRepository;

    public void saveFollowerHistory(Long instagramId, LocalDate date, long followerCount) {
        InstagramFollowerHistoryId id = new InstagramFollowerHistoryId(instagramId,
            date);

        InstagramFollowerHistoryEntity entity = new InstagramFollowerHistoryEntity(id,
            followerCount);

        instagramFollowerHistoryRepository.save(entity);
    }

    public List<InstagramFollowerHistoryResponseDto> findAllByInstagramIdRecently6Month(
        Long instagramId) {
        List<InstagramFollowerHistoryEntity> entities = instagramFollowerHistoryRepository.findById_InstagramIdAndId_MonthAfter(
            instagramId, DateTimeUtils.getStartOfCurrentMonth().minusMonths(7).toLocalDate());

        return entities.stream()
            .map(entity -> new InstagramFollowerHistoryResponseDto(
                entity.getId().getInstagramId(),
                entity.getId().getMonth().toString(),
                entity.getTotalFollowers()
            ))
            .toList();
    }
}
