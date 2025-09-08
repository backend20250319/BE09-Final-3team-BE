package site.petful.snsservice.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import site.petful.snsservice.batch.tasklet.InstagramTokenCleanupTasklet;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class InstagramTokenBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InstagramTokenCleanupTasklet tokenCleanupTasklet;


    @Bean
    public Job instagramTokenCleanupJob() {
        return new JobBuilder("instagramTokenCleanupJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(tokenCleanupStep())
            .build();
    }

    @Bean
    public Step tokenCleanupStep() {
        return new StepBuilder("tokenCleanup", jobRepository)
            .tasklet(tokenCleanupTasklet, transactionManager)
            .build();
    }
}
