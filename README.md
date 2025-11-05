# 🚀 웹 푸시 및 실시간 알림 백엔드 (Spring Boot)

이 프로젝트는 Spring Boot 백엔드 애플리케이션으로, 웹 푸시 알림 기능을 제공하고, Redis를 통한 구독 및 분산 세션 관리, Kafka를 통한 비동기 알림 처리, WebSocket와 Redis Pub/Sub을 통한 실시간 통신을 통합합니다.
**여기에 추가적으로 Spring Batch와 MySQL을 사용하여 크롤링 작업을 동적으로 스케줄링하고 관리하는 기능을 포함합니다.**

## ✨ 주요 기능

*   **웹 푸시 프로토콜 구현**: VAPID를 사용하여 구독된 클라이언트에 푸시 알림을 보냅니다.
*   **Redis 통합**: 웹 푸시 구독 정보 저장 및 여러 서버 인스턴스에 걸친 실시간 사용자 세션 관리를 수행합니다.
*   **Kafka 통합**: 확장 가능하고 분리된 메시징을 위해 알림 요청을 비동기적으로 처리합니다.
*   **분산 실시간 통신**: Spring WebSocket과 **Redis Pub/Sub**을 결합하여 여러 백엔드 인스턴스 환경에서도 특정 사용자에게 실시간 메시지를 안정적으로 전송합니다.
*   **동적 크롤링 스케줄링**: **Spring Batch와 MySQL**을 사용하여 Cron 표현식 기반의 크롤링 작업을 동적으로 예약, 관리, 취소합니다.
*   **VAPID 키 관리**: 푸시 서비스 인증을 위해 VAPID 공개 및 개인 키를 안전하게 처리합니다.
*   **비동기 처리**: Spring의 `@Async`를 활용하여 비차단 작업을 수행하고 애플리케이션 응답성을 향상시킵니다.

## 🛠️ 기술 스택

*   **프레임워크**: Spring Boot 3.3.0, **Spring Batch**
*   **언어**: Java 21
*   **빌드 도구**: Gradle
*   **메시징**: Apache Kafka, Redis Pub/Sub
*   **데이터 저장소**: Redis, **MySQL**
*   **Web Push 라이브러리**: `nl.martijndwars:web-push`
*   **실시간 통신**: Spring WebSocket
*   **유틸리티**: Lombok
*   **JSON 처리**: Jackson

## 📦 설치 및 실행

이 프로젝트를 실행하려면 Java 21, Gradle, Docker (Kafka 및 Redis용), **MySQL 데이터베이스**, 그리고 VAPID 키 쌍이 설치되어 있어야 합니다.

### 1. 저장소 클론

```bash
git clone <repository_url>
cd web-push-with-redis-kafka-be
```

### 2. Docker 설정 (Kafka & Redis)

프로젝트는 Kafka와 Redis에 의존합니다. Docker Compose를 사용하여 쉽게 실행할 수 있습니다.

프로젝트 루트 (`redis-kafka/docker-compose.yml`)에 다음 내용으로 `docker-compose.yml` 파일을 생성합니다:

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.1
    hostname: zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.0.1
    hostname: kafka
    container_name: kafka
    ports:
      - "9092:9092"
      - "9094:9094"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9094
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
    depends_on:
      - zookeeper

  redis:
    image: redis:7.0.11-alpine
    hostname: redis
    container_name: redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
```

그런 다음 Docker 컨테이너를 시작합니다:

```bash
docker-compose up -d
```

### 3. MySQL 데이터베이스 설정

크롤링 스케줄 정보 저장을 위해 MySQL 데이터베이스가 필요합니다. 로컬 MySQL 서버를 설치하거나 Docker를 사용할 수 있습니다.

**로컬 MySQL 설치**: MySQL 서버를 설치하고 데이터베이스 및 사용자를 생성해야 합니다.

```sql
CREATE DATABASE your_database_name CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON your_database_name.* TO 'your_username'@'localhost';
FLUSH PRIVILEGES;
```

**`your_database_name`, `your_username`, `your_password`**를 실제 사용할 값으로 대체하십시오.

### 4. VAPID 키 생성

웹 푸시에는 VAPID 키 쌍이 필요합니다. 온라인 도구나 Node.js 스크립트를 사용하여 생성할 수 있습니다.

```javascript
// vapid-key-generator.js
const webpush = require('web-push');
const vapidKeys = webpush.generateVAPIDKeys();
console.log('Public Key:', vapidKeys.publicKey);
console.log('Private Key:', vapidKeys.privateKey);
```

**중요**: 개인 키는 매우 민감하므로 안전하게 보관하고 클라이언트 측에 노출하지 마십시오.

### 5. `application.properties` 구성

`src/main/resources/application.properties` 파일을 열고 Kafka, Redis, Web Push VAPID 키 **및 MySQL 데이터베이스 설정**을 업데이트합니다:

```properties
# Kafka 설정
spring.kafka.bootstrap-servers=localhost:9094
spring.kafka.consumer.group-id=my-app-group
spring.kafka.consumer.auto-offset-reset=earliest

# Redis 설정
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Web Push VAPID 키
vapid.public.key=YOUR_VAPID_PUBLIC_KEY_HERE
vapid.private.key=YOUR_VAPID_PRIVATE_KEY_HERE

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA and Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

**`YOUR_VAPID_PUBLIC_KEY_HERE`, `YOUR_VAPID_PRIVATE_KEY_HERE`, `your_database_name`, `your_username`, `your_password`**를 실제 값으로 대체하십시오.

### 6. 애플리케이션 빌드 및 실행

```bash
./gradlew bootRun
```

애플리케이션은 `http://localhost:8080`에서 시작됩니다.

## 🎯 사용법

1.  **프론트엔드 구독**: 프론트엔드에서 알림 권한을 부여하고 푸시 알림을 구독합니다.
2.  **테스트 푸시 알림**: 프론트엔드 UI로 테스트 푸시 알림을 보냅니다. 백엔드가 Kafka로 메시지를 보내고, 소비되어 웹 푸시 알림으로 발송됩니다.
3.  **크롤링 시작**: 프론트엔드 UI로 크롤링 시뮬레이션을 시작합니다. 백엔드는 WebSocket을 통해 실시간 업데이트를 보내고 완료 시 최종 푸시 알림을 보냅니다.
**4. 크롤링 스케줄 관리**: 프론트엔드 UI의 스케줄 관리 섹션을 통해 Cron 표현식을 사용하여 크롤링 작업을 예약, 확인, 수정 또는 취소할 수 있습니다.

---

# 📚 아키텍처 및 코드 설명

## 1. 전체 아키텍처

### 흐름 개요

*   **웹 푸시 구독 흐름**:
    1.  프론트엔드가 `PushSubscription` 객체를 `/api/subscribe` 로 전송합니다.
    2.  `WebPushController`가 요청을 받아 `WebPushService`를 통해 구독 정보를 **Redis**에 저장합니다.

*   **웹 푸시 알림 발송 흐름**:
    1.  API (`/api/notifications`) 호출로 알림 메시지를 보냅니다.
    2.  `NotificationController`가 요청을 받아 `NotificationService`를 통해 메시지를 **Kafka**의 `notification-topic`으로 발행합니다.
    3.  `NotificationConsumer`가 Kafka 메시지를 소비합니다.
    4.  `WebPushService`는 **Redis**에 저장된 모든 구독 정보를 가져와 각 클라이언트에게 푸시 알림을 전송합니다.

*   **실시간 크롤링 알림 흐름 (WebSocket + Redis Pub/Sub)**:
    1.  **연결 수립**: 프론트엔드가 백엔드 인스턴스 중 하나와 `/ws/test/{userId}`로 WebSocket 연결을 맺습니다.
    2.  **세션 등록**: `WebSocketConnectionHandler`가 연결을 처리합니다.
        *   해당 인스턴스의 `WebSocketSessionManager`에 세션을 **로컬**로 저장합니다.
        *   전체 연결 사용자 추적을 위해 **Redis**의 `ws:users` Set에 `userId`를 추가합니다.
    3.  **크롤링 시작**: 프론트엔드가 `/api/crawling/start/{userId}`를 호출합니다.
    4.  **세션 확인 및 발행**: `CrawlingService`는 다음을 수행합니다.
        *   Redis의 `ws:users` Set을 통해 `userId`가 활성 세션을 가지고 있는지 확인합니다.
        *   크롤링 작업을 비동기로 시작하고, 진행 상황 메시지를 `RedisMessagePublisher`를 통해 `ws:crawling:{userId}` 채널로 **발행(Publish)**합니다.
    5.  **메시지 수신 및 전송**:
        *   `RedisMessageSubscriber`가 `ws:user:*` 및 `ws:crawling:*` 패턴의 채널을 **구독(Subscribe)**하고 있다가 메시지를 수신합니다.
        *   `MessageHandlerFactory`를 통해 `CrawlingProgressMessageHandler`와 같은 적절한 핸들러를 찾아 메시지를 처리합니다.
        *   `CrawlingProgressMessageHandler`는 수신된 크롤링 진행 메시지를 `WebSocketSessionManager`를 통해 해당 **로컬 세션**으로 전송합니다.
    6.  **연결 종료**: 연결이 끊어지면 `WebSocketConnectionHandler`는 로컬 세션을 제거하고 Redis의 `ws:users` Set에서도 `userId`를 제거합니다.

*   **크롤링 스케줄링 흐름 (Spring Batch + MySQL + 동적 스케줄러)**:
    1.  **스케줄 등록/관리**: 프론트엔드에서 새로운 크롤링 스케줄(사용자 ID, Cron 표현식)을 백엔드의 `/api/schedules/crawling` 엔드포인트로 전송합니다.
    2.  **서비스 처리**: `CrawlingScheduleController`가 요청을 받아 `CrawlingScheduleService`에 위임합니다.
    3.  **DB 저장 및 스케줄링**: `CrawlingScheduleService`는 스케줄 정보를 **MySQL 데이터베이스**의 `crawling_job_schedule` 테이블에 저장하고, {@link DynamicScheduler}를 통해 해당 Cron 표현식에 따라 작업을 예약합니다.
    4.  **배치 Job 실행**: 예약된 시각이 되면 {@link DynamicScheduler}는 Spring Batch의 {@link JobLauncher}를 사용하여 미리 정의된 `crawlingJob` ({@link CrawlingJobConfig}에 정의)을 실행합니다.
    5.  **크롤링 로직**: `crawlingJob` 내의 {@link Tasklet}은 {@link CrawlingService}를 호출하여 실제 크롤링 시뮬레이션 및 WebSocket을 통한 실시간 알림을 보냅니다.
    6.  **재시작 복구**: 애플리케이션 재시작 시, `CrawlingScheduleService`의 `@PostConstruct` 메서드는 MySQL에 저장된 모든 **SCHEDULED** 상태의 작업을 다시 로드하여 {@link DynamicScheduler}에 등록합니다.

## 2. 주요 파일 및 패키지

### `com.mytoyappbe.config` 패키지

*   `RedisConfig.java`: Redis 연결과 `RedisTemplate`을 구성합니다. 또한, **`RedisMessageListenerContainer`**를 설정하여 `RedisMessageSubscriber`를 특정 Redis 채널 패턴(`ws:user:*`, `ws:crawling:*`)의 리스너로 등록합니다.
*   `WebSocketConfig.java`: `/ws/test/{userId}` 경로에 `TestWebSocketHandler`를 등록하여 WebSocket 엔드포인트를 활성화합니다.

### `com.mytoyappbe.entity` 패키지

*   `CrawlingJobSchedule.java`: 크롤링 스케줄 작업의 정보를 저장하는 JPA 엔티티입니다. Cron 표현식, 사용자 ID, 작업 이름, 상태 등 스케줄 관련 모든 정보를 포함합니다.

### `com.mytoyappbe.repository` 패키지

*   `CrawlingJobScheduleRepository.java`: `CrawlingJobSchedule` 엔티티에 대한 데이터베이스 접근(CRUD)을 처리하는 Spring Data JPA 리포지토리입니다.

### `com.mytoyappbe.batch` 패키지

*   `CrawlingJobConfig.java`: Spring Batch의 크롤링 작업을 정의합니다. 실제 크롤링 로직은 {@link CrawlingService}를 호출하는 {@link Tasklet} 내에서 실행됩니다.

### `com.mytoyappbe.scheduler` 패키지

*   `DynamicScheduler.java`: {@link ThreadPoolTaskScheduler}를 사용하여 Cron 표현식에 따라 Spring Batch 작업을 동적으로 스케줄링, 실행, 취소하는 핵심 컴포넌트입니다.

### `com.mytoyappbe.websocket.handler` 패키지

*   `WebSocketConnectionHandler.java`: WebSocket 연결, 메시지, 종료 이벤트를 처리합니다. 연결이 수립되면 로컬 세션을 `WebSocketSessionManager`에 추가하고, 분산 환경에서 사용자 연결 상태를 관리하기 위해 **Redis Set에 `userId`를 추가**합니다.
*   `MessageHandler.java`: Redis Pub/Sub 메시지를 처리하기 위한 인터페이스입니다.
*   `MessageHandlerFactory.java`: `MessageHandler` 구현체들을 관리하고, 채널에 따라 적절한 핸들러를 반환합니다.
*   `CrawlingProgressMessageHandler.java`: `ws:crawling:*` 채널에서 수신된 크롤링 진행 메시지를 처리하고, 해당 사용자에게 WebSocket을 통해 전달합니다.

### `com.mytoyappbe.manager` 패키지

*   `WebSocketSessionManager.java`: **로컬 인스턴스 내**의 활성 WebSocket 세션만 관리합니다. 세션을 추가, 제거하고 특정 로컬 세션으로 메시지를 보내는 메서드를 제공합니다.

### `com.mytoyappbe.service` 패키지

*   `CrawlingService.java`: 모의 웹 크롤링 로직을 구현합니다. Redis를 통해 사용자의 활성 세션 존재 여부를 확인한 후, **`RedisMessagePublisher`**를 사용하여 크롤링 진행 상황을 Redis Pub/Sub 채널로 발행합니다.
*   `NotificationService.java`: 알림 메시지를 Kafka에 발행하는 역할을 합니다.
*   `WebPushService.java`: 웹 푸시 구독 정보를 Redis에 저장하고, 모든 구독자에게 푸시 알림을 전송합니다.

### `com.mytoyappbe.service.pubsub` 패키지

*   `RedisMessagePublisher.java`: `RedisTemplate.convertAndSend()`를 사용하여 특정 Redis 채널에 메시지를 발행하는 서비스입니다.
*   `RedisMessageSubscriber.java`: Redis의 `MessageListener`를 구현한 서비스입니다. `RedisConfig`에 등록되어 특정 채널 패턴을 구독하며, 메시지를 수신하면 `WebSocketSessionManager`를 통해 해당 사용자의 로컬 WebSocket 세션으로 메시지를 전달합니다.

### `com.mytoyappbe.service.schedule` 패키지

*   `CrawlingScheduleService.java`: 크롤링 작업 스케줄의 비즈니스 로직을 처리합니다. 데이터베이스에 스케줄 정보를 저장하고 {@link DynamicScheduler}를 통해 스케줄링 작업을 관리합니다.

### `com.mytoyappbe.controller` 패키지

*   `NotificationController.java`: 알림 전송을 위한 REST 컨트롤러입니다. 알림 요청을 수신하는 엔드포인트 (`/api/notifications`)를 노출하고 `NotificationService`를 통해 Kafka에 게시합니다.
*   `WebPushController.java`: 웹 푸시 구독 관리를 위한 REST 컨트롤러입니다. 프론트엔드로부터 `PushSubscriptionDto`를 수신하는 엔드포인트 (`/api/subscribe`)를 노출하고 `WebPushService`를 사용하여 이를 저장합니다.

### `com.mytoyappbe.controller.schedule` 패키지

*   `CrawlingScheduleController.java`: 크롤링 작업 스케줄을 관리하기 위한 REST API를 제공합니다. 스케줄 추가, 조회, 수정, 취소 엔드포인트를 노출합니다.

### 기타 주요 패키지

*   `com.mytoyappbe.consumer`: Kafka 메시지를 소비하는 컨슈머를 포함합니다.
*   `com.mytoyappbe.dto`: 데이터 전송 객체(DTO)를 포함합니다.
