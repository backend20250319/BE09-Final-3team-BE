package site.petful.snsservice.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import site.petful.snsservice.batch.listener.InstagramBatchJobListener;
import site.petful.snsservice.batch.listener.InstagramBatchStepListener;
import site.petful.snsservice.batch.tasklet.InstagramCommentSyncTasklet;
import site.petful.snsservice.batch.tasklet.InstagramInsightSyncTasklet;
import site.petful.snsservice.batch.tasklet.InstagramMediaSyncTasklet;
import site.petful.snsservice.batch.tasklet.InstagramProfileSyncTasklet;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class InstagramBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InstagramBatchJobListener jobListener;
    private final InstagramBatchStepListener stepListener;
    private final InstagramProfileSyncTasklet profileSyncTasklet;
    private final InstagramMediaSyncTasklet mediaSyncTasklet;
    private final InstagramCommentSyncTasklet commentSyncTasklet;
    private final InstagramInsightSyncTasklet insightSyncTasklet;

    @Bean
    public Job instagramSyncJob() {
        return new JobBuilder("instagramSyncJob", jobRepository)
            .listener(jobListener)
            .start(profileSyncStep())
            .next(mediaSyncStep())
            .next(insightSyncStep())
            .next(commentSyncStep())
            .build();
    }

    @Bean
    public Step profileSyncStep() {
        return new StepBuilder("profileSyncStep", jobRepository)
            .tasklet(profileSyncTasklet, transactionManager)
            .listener(stepListener)
            .build();
    }

    @Bean
    public Step mediaSyncStep() {
        return new StepBuilder("mediaSyncStep", jobRepository)
            .tasklet(mediaSyncTasklet, transactionManager)
            .listener(stepListener)
            .build();
    }

    @Bean
    public Step commentSyncStep() {
        return new StepBuilder("commentSyncStep", jobRepository)
            .tasklet(commentSyncTasklet, transactionManager)
            .listener(stepListener)
            .build();
    }

    @Bean
    public Step insightSyncStep() {
        return new StepBuilder("insightSyncStep", jobRepository)
            .tasklet(insightSyncTasklet, transactionManager)
            .listener(stepListener)
            .build();
    }
}
