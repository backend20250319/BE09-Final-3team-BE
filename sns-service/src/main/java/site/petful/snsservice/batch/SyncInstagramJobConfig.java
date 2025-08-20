package site.petful.snsservice.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import site.petful.snsservice.instagram.service.InstagramService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SyncInstagramJobConfig {

    private final InstagramService instagramService;

}
