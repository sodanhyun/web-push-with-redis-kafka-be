# Spring Batch 가이드: 초기화, 동작 방식 및 사용법

## 1. 서론

이 문서는 `web-push-with-redis-kafka-be` 프로젝트에 통합된 Spring Batch의 설정, 동작 방식, 주요 개념 및 사용법을 설명합니다. Spring Batch는 대용량 데이터 처리를 위한 강력한 프레임워크로, 이 프로젝트에서는 Cron 표현식 기반의 동적 크롤링 스케줄링 및 실행에 활용됩니다.

## 2. 주요 개념 및 용어

Spring Batch는 다음과 같은 핵심 개념들을 기반으로 동작합니다.

*   **Job (작업)**: 전체 배치 프로세스를 캡슐화하는 엔티티입니다. 하나 이상의 {@link Step}으로 구성됩니다.
*   **Step (단계)**: Job 내의 독립적인 순차적 페이즈를 나타냅니다. 실제 배치 처리 로직이 정의되는 곳입니다. {@link Tasklet} 기반 또는 청크(Chunk) 기반으로 구성될 수 있습니다.
*   **Tasklet (태스크릿)**: 단일 Step 내에서 실행되는 가장 간단한 형태의 처리 단위입니다. 복잡하지 않은 단일 작업을 수행할 때 유용합니다. 이 프로젝트의 크롤링 작업은 Tasklet 기반으로 구현됩니다.
*   **JobRepository (작업 저장소)**: Job의 실행에 필요한 메타데이터(Job 실행 기록, Step 실행 기록, Job 파라미터 등)를 저장하고 관리합니다. 이 프로젝트에서는 MySQL 데이터베이스를 사용합니다.
*   **JobLauncher (작업 실행기)**: Job을 실행하는 인터페이스입니다. Job과 JobParameters를 받아 JobExecution을 시작합니다.
*   **JobExplorer (작업 탐색기)**: JobRepository에 저장된 메타데이터를 조회하는 인터페이스입니다.
*   **JobParameters (작업 파라미터)**: Job 실행 시 전달되는 파라미터로, 특정 JobInstance를 고유하게 식별하는 데 사용됩니다. 이 프로젝트에서는 `userId`를 Job Parameters로 전달하여 사용자별 크롤링 작업을 식별합니다.
*   **JobInstance (작업 인스턴스)**: 특정 Job과 JobParameters의 조합을 나타냅니다. 동일한 Job이라도 JobParameters가 다르면 다른 JobInstance로 간주됩니다.
*   **JobExecution (작업 실행)**: JobInstance의 단일 실행 시도를 나타냅니다. JobInstance가 실패하면 여러 JobExecution을 가질 수 있습니다.
*   **StepExecution (단계 실행)**: Step의 단일 실행 시도를 나타냅니다.
*   **Cron 표현식**: 특정 시간(초, 분, 시, 일, 월, 요일)에 작업을 예약하기 위한 문자열 형식입니다. Spring Batch의 {@link CronTrigger}는 6필드(초 분 시 일 월 요일) 형식을 사용합니다.

## 3. 프로젝트 내 Spring Batch 구성

### `com.mytoyappbe.config.BatchConfig.java`

*   **역할**: Spring Batch의 메타데이터 테이블을 데이터베이스에 자동으로 생성하도록 설정합니다.
*   **주요 내용**: `@EnableBatchProcessing` 어노테이션을 통해 Spring Batch 기능을 활성화하고, `batchDataSourceInitializer` 빈을 정의하여 `schema-mysql.sql` 스크립트를 실행합니다. 이 스크립트는 `BATCH_JOB_INSTANCE`, `BATCH_STEP_EXECUTION` 등 Spring Batch 운영에 필요한 테이블들을 생성합니다.

### `com.mytoyappbe.batch.CrawlingJobConfig.java`

*   **역할**: 실제 크롤링 작업을 위한 Spring Batch {@link Job}과 {@link Step}을 정의합니다.
*   **주요 내용**:
    *   `crawlingTasklet()`: {@link Tasklet} 빈을 정의합니다. 이 Tasklet은 Job Parameters에서 `userId`를 추출하여 {@link CrawlingService#startCrawling(String)} 메서드를 호출합니다. 실제 크롤링 로직은 `CrawlingService`에 위임됩니다.
    *   `crawlingStep()`: `crawlingTasklet`을 실행하는 단일 {@link Step}을 정의합니다.
    *   `crawlingJob()`: `crawlingStep`을 포함하는 {@link Job}을 정의합니다. `RunIdIncrementer`를 사용하여 Job 실행 시마다 고유한 JobInstance ID를 부여하며, {@link CrawlingJobCompletionNotificationListener}를 등록하여 Job 완료/실패 시 알림을 처리합니다.

### `com.mytoyappbe.scheduler.DynamicScheduler.java`

*   **역할**: Cron 표현식에 따라 Spring Batch Job을 동적으로 스케줄링하고 실행을 관리하는 핵심 컴포넌트입니다.
*   **주요 내용**:
    *   `ThreadPoolTaskScheduler`를 사용하여 Cron 표현식에 따라 Job을 예약합니다.
    *   `JobLauncher`를 주입받아 예약된 시각에 `crawlingJob`을 실행합니다.
    *   애플리케이션 시작 시 MySQL에 저장된 모든 `SCHEDULED` 상태의 Job을 로드하여 다시 스케줄링하는 복구 로직을 포함합니다.

### `com.mytoyappbe.batch.CrawlingJobCompletionNotificationListener.java`

*   **역할**: Spring Batch Job의 실행 완료(성공 또는 실패) 이벤트를 감지하고 처리합니다.
*   **주요 내용**: `JobExecutionListener` 인터페이스를 구현하며, `afterJob` 메서드에서 Job의 상태를 확인합니다. Job이 성공적으로 완료되면 Job Parameters에서 `userId`를 추출하여 {@link NotificationService}를 통해 사용자에게 웹 푸시 알림을 전송합니다. Job 실패 시에도 알림을 보낼 수 있도록 구현되어 있습니다.

## 4. 동작 방식

1.  **스케줄 등록**: 프론트엔드에서 사용자 ID와 Cron 표현식을 포함한 크롤링 스케줄 요청을 백엔드의 `/api/schedules/crawling` 엔드포인트로 전송합니다.
2.  **서비스 처리**: {@link CrawlingScheduleController}가 요청을 받아 {@link CrawlingScheduleService}에 위임합니다.
3.  **DB 저장 및 스케줄링**: `CrawlingScheduleService`는 스케줄 정보를 MySQL 데이터베이스의 `crawling_job_schedule` 테이블에 저장하고, {@link DynamicScheduler}를 통해 해당 Cron 표현식에 따라 `crawlingJob`을 예약합니다.
4.  **예약된 Job 실행**: 예약된 시각이 되면 `DynamicScheduler`는 `JobLauncher`를 사용하여 `crawlingJob`을 실행합니다. 이때 `userId`가 Job Parameters로 전달됩니다.
5.  **크롤링 로직 수행**: `crawlingJob` 내의 `crawlingTasklet`이 실행되고, 이 Tasklet은 {@link CrawlingService#startCrawling(String)} 메서드를 호출합니다. `CrawlingService`는 크롤링 시뮬레이션을 수행하며, 진행 상황을 Redis Pub/Sub을 통해 WebSocket으로 실시간 업데이트합니다.
6.  **Job 완료 알림**: `crawlingJob`이 성공적으로 완료되면, `CrawlingJobCompletionNotificationListener`의 `afterJob` 메서드가 호출됩니다. 이 리스너는 Job Parameters에서 `userId`를 가져와 {@link NotificationService}를 통해 사용자에게 크롤링 완료 웹 푸시 알림을 전송합니다.
7.  **재시작 복구**: 애플리케이션 재시작 시, `CrawlingScheduleService`의 `@PostConstruct` 메서드는 MySQL에 저장된 모든 `SCHEDULED` 상태의 Job을 로드하여 `DynamicScheduler`에 다시 등록함으로써 스케줄의 영속성을 보장합니다.

## 5. 사용 방법 (개발자 관점)

*   **`application.properties` 설정**: MySQL 데이터베이스 연결 정보와 함께 Spring Batch 메타데이터 테이블 초기화를 위한 설정이 필요합니다. 이 프로젝트에서는 `BatchConfig.java`를 통해 명시적으로 스키마를 초기화하므로, `spring.batch.jdbc.initialize-schema` 관련 설정은 필요하지 않습니다.
*   **Job Parameters 전달**: Job을 실행할 때 `JobParameters`를 통해 필요한 데이터를 전달할 수 있습니다. 이 프로젝트에서는 `userId`를 전달하여 특정 사용자와 관련된 작업을 수행합니다.
*   **Job 모니터링**: Spring Batch는 `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION` 등 다양한 메타데이터 테이블에 Job 실행 정보를 기록합니다. 이 테이블들을 조회하여 Job의 상태, 실행 시간, 성공/실패 여부 등을 모니터링할 수 있습니다.

## 6. 결론

Spring Batch는 이 프로젝트에서 동적 크롤링 스케줄링을 위한 견고하고 확장 가능한 솔루션을 제공합니다. Job, Step, Tasklet과 같은 핵심 개념을 이해하고, `BatchConfig`, `CrawlingJobConfig`, `DynamicScheduler`, `CrawlingJobCompletionNotificationListener`의 역할을 파악하면 효과적인 배치 작업 관리가 가능합니다.