package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.client.SnSFeignClient;
import site.petful.campaignservice.dto.InstagramProfileDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstagramService {

    private final SnSFeignClient snsFeignClient;

    public List<InstagramProfileDto> getProfile() {
        return snsFeignClient.getProfiles().getData();
    }
}
