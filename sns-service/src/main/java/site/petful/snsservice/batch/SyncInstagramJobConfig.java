package site.petful.snsservice.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import site.petful.snsservice.instagram.service.InstagramService;


// TODO 나중에 마무리에 배치 작업
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SyncInstagramJobConfig {

    private final InstagramService instagramService;

}
