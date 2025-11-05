package com.mytoyappbe.batch;

import com.mytoyappbe.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch를 사용하여 크롤링 작업을 정의하고 설정하는 클래스입니다.
 * <p>
 * {@code @EnableBatchProcessing} 어노테이션을 통해 Spring Batch의 핵심 기능(JobRepository, JobLauncher 등)을 활성화하며,
 * 크롤링 작업을 위한 {@link Job}과 {@link Step} 빈을 생성합니다.
 * 이 Job은 {@link CrawlingService}를 호출하여 실제 크롤링 로직을 실행합니다.
 */
@Slf4j
@Configuration
@EnableBatchProcessing // Spring Batch 기능을 활성화하고 필요한 인프라 빈들을 자동으로 등록합니다.
@RequiredArgsConstructor
public class CrawlingJobConfig {

    /**
     * Spring Batch의 Job 메타데이터를 저장하고 관리하는 리포지토리입니다.
     * Job 실행 기록, Step 실행 기록, Job 파라미터 등을 데이터베이스에 저장합니다.
     */
    private final JobRepository jobRepository;

    /**
     * 트랜잭션 관리를 위한 Spring의 {@link PlatformTransactionManager}입니다。
     * 배치 작업의 각 Step은 트랜잭션 경계 내에서 실행됩니다.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * 실제 크롤링 비즈니스 로직을 포함하는 서비스입니다.
     * 배치 Job에서 이 서비스를 호출하여 크롤링을 시작합니다.
     */
    private final CrawlingService crawlingService;

    /**
     * Job 완료 후 알림을 전송하는 리스너입니다.
     */
    private final CrawlingJobCompletionNotificationListener crawlingJobCompletionNotificationListener;

    /**
     * 크롤링 작업을 수행하는 {@link Tasklet}을 정의하는 빈입니다.
     * <p>
     * {@link Tasklet}은 단일 Step 내에서 실행되는 가장 간단한 형태의 처리 단위입니다.
     * 여기서는 {@link CrawlingService#startCrawling(String)} 메서드를 호출하여
     * 실제 크롤링 로직을 실행하고, Job 파라미터로 전달된 {@code userId}를 사용합니다.
     *
     * @return 크롤링 작업을 실행하는 {@link Tasklet} 인스턴스
     */
    @Bean
    public Tasklet crawlingTasklet() {
        return (contribution, chunkContext) -> {
            // Job Parameters에서 "userId"를 추출합니다.
            // Job Parameters는 Job을 실행할 때 외부에서 전달되는 값으로, JobInstance를 고유하게 식별하는 데 사용됩니다.
            String userId = (String) chunkContext.getStepContext().getJobParameters().get("userId");
            if (userId == null) {
                log.error("Crawling Tasklet: userId job parameter is missing. Job will not proceed.");
                // Job 파라미터가 누락된 경우, 작업을 실패로 처리하거나 추가적인 로직을 구현할 수 있습니다.
                return RepeatStatus.FINISHED; // 작업 완료 상태를 반환합니다.
            }
            log.info("Crawling Tasklet: Starting crawling for user: {}", userId);
            // 실제 크롤링 비즈니스 로직을 호출합니다。
            crawlingService.startCrawling(userId);
            log.info("Crawling Tasklet: Finished crawling for user: {}", userId);
            return RepeatStatus.FINISHED; // Tasklet이 성공적으로 완료되었음을 나타냅니다.
        };
    }

    /**
     * 크롤링 작업을 위한 {@link Step}을 정의하는 빈입니다.
     * <p>
     * {@link Step}은 Job의 독립적인 순차적 페이즈를 나타냅니다.
     * 여기서는 {@link #crawlingTasklet()}을 실행하는 단일 Tasklet 기반 Step으로 구성됩니다.
     *
     * @return 크롤링 작업을 위한 {@link Step} 인스턴스
     */
    @Bean
    public Step crawlingStep() {
        return new StepBuilder("crawlingStep", jobRepository) // Step의 이름과 JobRepository를 설정합니다.
                .tasklet(crawlingTasklet(), transactionManager) // 이 Step에서 실행할 Tasklet과 트랜잭션 매니저를 연결합니다.
                // .chunk(10): Tasklet 대신 ItemReader, ItemProcessor, ItemWriter를 사용하는 청크 기반 처리 시 사용됩니다.
                // 청크 기반 처리는 대량의 데이터를 일괄 처리할 때 효율적입니다.
                .build();
    }

    /**
     * 크롤링 작업을 위한 {@link Job}을 정의하는 빈입니다.
     * <p>
     * {@link Job}은 전체 배치 프로세스를 캡슐화하는 엔티티입니다.
     * 이 Job은 {@link #crawlingStep()}을 포함하며, Job 실행 시마다 고유한 JobInstance ID를 부여합니다.
     *
     * @return 크롤링 작업을 위한 {@link Job} 인스턴스
     */
    @Bean
    public Job crawlingJob() {
        return new JobBuilder("crawlingJob", jobRepository) // Job의 이름과 JobRepository를 설정합니다.
                .incrementer(new RunIdIncrementer()) // Job 실행 시마다 JobInstance ID를 자동으로 증가시켜 JobInstance를 고유하게 만듭니다.
                // JobParameters를 통해 JobInstance를 고유하게 식별할 수 있습니다.
                .listener(crawlingJobCompletionNotificationListener) // Job 완료 리스너를 등록합니다.
                .start(crawlingStep()) // Job의 첫 번째 Step으로 crawlingStep을 설정합니다.
                // .next(anotherStep()): 여러 Step을 순차적으로 연결할 수 있습니다.
                .build();
    }
}
