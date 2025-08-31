package site.petful.snsservice.instagram.profile.service;

import com.jayway.jsonpath.JsonPath;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.snsservice.instagram.client.InstagramApiClient;
import site.petful.snsservice.instagram.insight.service.InstagramFollowerHistoryService;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;
import site.petful.snsservice.instagram.profile.repository.InstagramProfileRepository;
import site.petful.snsservice.util.DateTimeUtils;

@Service
@RequiredArgsConstructor
public class InstagramProfileService {

    private final String fields = "username,name,profile_picture_url,biography,followers_count,follows_count,media_count,website";
    private final InstagramProfileRepository instagramProfileRepository;
    private final InstagramApiClient instagramApiClient;
    private final InstagramFollowerHistoryService instagramFollowerHistoryService;


    public List<InstagramProfileDto> syncAllInstagramProfiles(Long userNo,
        String accessToken) {
        // TODO [예외처리] null일때 오류 처리
        String jsonString = instagramApiClient.fetchAccounts(accessToken);
        List<String> instagramIds = JsonPath.read(jsonString,
            "$.data[*].instagram_business_account.id");

        List<InstagramProfileDto> profiles = new ArrayList<>();
        for (String instagramId : instagramIds) {
            InstagramProfileDto instagramProfileDto = syncSingleInstagramProfile(
                userNo,
                Long.parseLong(instagramId), accessToken);

            profiles.add(instagramProfileDto);
        }

        for (InstagramProfileDto profile : profiles) {
            instagramFollowerHistoryService.saveFollowerHistory(profile.id(),
                DateTimeUtils.getStartOfCurrentMonth().toLocalDate(), profile.followers_count());
        }

        return profiles;
    }

    public InstagramProfileDto syncSingleInstagramProfile(Long userId, Long instagramId,
        String accessToken) {

        InstagramProfileDto response = instagramApiClient.fetchProfile(instagramId,
            accessToken, fields);

        Optional<InstagramProfileEntity> existingProfile = instagramProfileRepository.findById(
            response.id());

        if (existingProfile.isPresent()) {
            InstagramProfileEntity profile = existingProfile.get();
            profile.updateFromDto(response);
            instagramProfileRepository.save(profile);

            return InstagramProfileDto.fromEntity(profile);
        }

        InstagramProfileEntity profile = new InstagramProfileEntity(response, userId, false);

        profile = instagramProfileRepository.save(profile);

        return InstagramProfileDto.fromEntity(profile);
    }


    public List<InstagramProfileDto> getProfiles(Long userNo) {
        List<InstagramProfileEntity> entities = instagramProfileRepository.findAllByUserNo(
            userNo);

        return entities.stream().map(InstagramProfileDto::fromEntity).toList();
    }

    public InstagramProfileDto getProfile(Long instagramId) {
        InstagramProfileEntity entity = instagramProfileRepository.findById(instagramId)
            .orElseThrow(
                () -> new NotFoundException("해당 인스타그램의 id를 찾을 수 없습니다.")
            );

        return InstagramProfileDto.fromEntity(entity);
    }

    @Transactional
    public void setAutoDelete(Long userNo, Long instagramId, Boolean isAutoDelete) {
        InstagramProfileEntity entity = instagramProfileRepository.findById(instagramId)
            .orElseThrow(
                () -> new NotFoundException("해당 인스타그램의 id를 찾을 수 없습니다.")
            );

        if (entity.getUserNo() != userNo) {
            throw new NotFoundException("해당 유저의 인스타그램이 아닙니다.");
        }

        entity.setAutoDelete(isAutoDelete);
        instagramProfileRepository.save(entity);
    }
}
