/**
 * @file CrawlingJobConfig.java
 * @description Spring Batch를 사용하여 크롤링 작업을 정의하고 설정하는 클래스입니다.
 *              {@code @EnableBatchProcessing} 어노테이션을 통해 Spring Batch의 핵심 기능을 활성화하며,
 *              크롤링 작업을 위한 {@link Job}과 {@link Step} 빈을 생성합니다.
 *              이 Job은 {@link com.mytoyappbe.crawling.service.CrawlingService}를 호출하여 실제 크롤링 로직을 실행합니다.
 */

package com.mytoyappbe.schedule.config;



import com.mytoyappbe.crawling.service.CrawlingService;

import com.mytoyappbe.schedule.listener.CrawlingJobListener;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;

import org.springframework.batch.core.Step;

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

 * @class CrawlingJobConfig

 * @description Spring Batch를 사용하여 크롤링 작업을 정의하고 설정하는 클래스입니다.

 *              크롤링 작업을 위한 `Job`과 `Step` 빈을 생성합니다.

 */

@Slf4j

@Configuration

@RequiredArgsConstructor

public class CrawlingJobConfig {



    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final CrawlingService crawlingService;

    private final CrawlingJobListener crawlingJobListener;



    /**

     * @method crawlingTasklet

     * @description 크롤링 작업을 수행하는 {@link Tasklet}을 정의하는 빈입니다.

     *              `Tasklet`은 단일 Step 내에서 실행되는 가장 간단한 형태의 처리 단위입니다.

     *              여기서는 `CrawlingService#startCrawling(String)` 메서드를 호출하여

     *              실제 크롤링 로직을 실행하고, Job 파라미터로 전달된 `userId`를 사용합니다.

     * @return 크롤링 작업을 실행하는 Tasklet 인스턴스

     */

    @Bean

    public Tasklet crawlingTasklet() {

        return (contribution, chunkContext) -> {

            String userId = (String) chunkContext.getStepContext().getJobParameters().get("userId");

            if (userId == null) {

                log.error("Crawling Tasklet: userId job parameter is missing. Job will not proceed.");

                return RepeatStatus.FINISHED;

            }

            log.info("Crawling Tasklet: Starting crawling for user: {}", userId);

            crawlingService.startCrawling(userId);

            log.info("Crawling Tasklet: Finished crawling for user: {}", userId);

            return RepeatStatus.FINISHED;

        };

    }



    /**

     * @method crawlingStep

     * @description 크롤링 작업을 위한 {@link Step}을 정의하는 빈입니다.

     *              `Step`은 Job의 독립적인 순차적 페이즈를 나타냅니다.

     *              여기서는 {@link #crawlingTasklet()}을 실행하는 단일 Tasklet 기반 Step으로 구성됩니다.

     * @return 크롤링 작업을 위한 Step 인스턴스

     */

    @Bean

    public Step crawlingStep() {

        return new StepBuilder("crawlingStep", jobRepository)

                .tasklet(crawlingTasklet(), transactionManager)

                .build();

    }



    /**

     * @method crawlingJob

     * @description 크롤링 작업을 위한 {@link Job}을 정의하는 빈입니다.

     *              `Job`은 전체 배치 프로세스를 캡슐화하는 엔티티입니다.

     *              이 Job은 {@link #crawlingStep()}을 포함하며, Job 실행 시마다 고유한 JobInstance ID를 부여합니다.

     * @return 크롤링 작업을 위한 Job 인스턴스

     */

    @Bean

    public Job crawlingJob() {

        return new JobBuilder("crawlingJob", jobRepository)

                .incrementer(new RunIdIncrementer())

                .listener(crawlingJobListener)

                .start(crawlingStep())

                .build();

    }

}
