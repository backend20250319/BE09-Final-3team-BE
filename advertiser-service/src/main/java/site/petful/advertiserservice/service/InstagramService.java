package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.client.SnSFeignClient;
import site.petful.advertiserservice.dto.InstagramProfileDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstagramService {

    public final SnSFeignClient snsFeignClient;

    // 1. 사용자 profile 조회
    public List<InstagramProfileDto> getProfile(Long userNo) {
        return snsFeignClient.getProfileExternal(userNo).getData();
    }

    // 2. 사용자 profile 조회 by snsId
    public InstagramProfileDto getProfileBySnSId(Long snsId) {
        return snsFeignClient.getProfileBySnsId(snsId).getData();
    }
}
