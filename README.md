# 🚀 웹 푸시 및 실시간 알림 백엔드 (Spring Boot) & 프론트엔드 (React)

이 프로젝트는 Spring Boot 백엔드와 React 프론트엔드로 구성되어 있으며, 웹 푸시 알림, 실시간 통신, 동적 크롤링 스케줄링 기능을 제공합니다.

## 🌟 프로젝트 개요

### 백엔드 (Spring Boot)

Spring Boot 백엔드 애플리케이션은 웹 푸시 알림 기능을 제공하고, Redis를 통한 구독 및 분산 세션 관리, Kafka를 통한 비동기 알림 처리, **STOMP over WebSocket과 Spring Session Redis를 통한 분산 실시간 통신**을 통합합니다. 여기에 추가적으로 Spring Batch와 MySQL을 사용하여 크롤링 작업을 동적으로 스케줄링하고 관리하는 기능을 포함합니다.

### 프론트엔드 (React)

React 프론트엔드는 백엔드와 연동하여 사용자에게 푸시 알림 구독/해지, 실시간 크롤링 진행 상황 모니터링, 크롤링 스케줄 관리 등의 기능을 제공합니다. 사용자 경험(UX)과 코드의 확장성, 유지보수성을 고려하여 컴포넌트 및 훅 기반으로 설계되었습니다.

## ✨ 주요 기능

### 백엔드

*   **웹 푸시 프로토콜 구현**: VAPID를 사용하여 구독된 클라이언트에 푸시 알림을 보냅니다.
*   **Redis 통합**: 웹 푸시 구독 정보 저장 및 여러 서버 인스턴스에 걸친 실시간 사용자 세션 관리를 수행합니다.
*   **Kafka 통합**: 확장 가능하고 분리된 메시징을 위해 알림 요청을 비동기적으로 처리합니다.
*   **분산 실시간 통신**: **Spring STOMP over WebSocket과 Spring Session Redis**를 결합하여 여러 백엔드 인스턴스 환경에서도 특정 사용자에게 실시간 메시지를 안정적으로 전송합니다.
*   **동적 크롤링 스케줄링**: **Spring Batch와 MySQL**을 사용하여 Cron 표현식 기반의 크롤링 작업을 동적으로 예약, 관리, 취소합니다.
*   **VAPID 키 관리**: 푸시 서비스 인증을 위해 VAPID 공개 및 개인 키를 안전하게 처리합니다.
*   **비동기 처리**: Spring의 `@Async`를 활용하여 비차단 작업을 수행하고 애플리케이션 응답성을 향상시킵니다.

### 프론트엔드

*   **푸시 알림 관리**: `usePushNotification` 훅을 통해 알림 권한 요청, 구독, 해지 기능을 제공합니다.
*   **실시간 웹소켓 통신**: `useWebSocketStore`를 통해 백엔드와 실시간으로 데이터를 주고받으며 크롤링 진행 상황을 모니터링합니다.
*   **크롤링 스케줄 UI**: 백엔드의 크롤링 스케줄 관리 API와 연동하여 스케줄을 추가, 조회, 수정, 취소하는 사용자 인터페이스를 제공합니다.
*   **전역 상태 관리**: Zustand를 사용하여 `userId`와 같은 공통 상태를 효율적으로 관리하여 컴포넌트 간의 결합도를 낮춥니다.

## 🛠️ 기술 스택

### 백엔드

*   **프레임워크**: Spring Boot 3.3.0, Spring Batch
*   **언어**: Java 21
*   **빌드 도구**: Gradle
*   **메시징**: Apache Kafka, **Spring Session Redis**
*   **데이터 저장소**: Redis, MySQL
*   **Web Push 라이브러리**: `nl.martijndwars:web-push`
*   **실시간 통신**: **Spring STOMP over WebSocket (Spring Session Redis, Reactor Netty)**
*   **유틸리티**: Lombok
*   **JSON 처리**: Jackson

### 프론트엔드

*   **프레임워크**: React 19
*   **언어**: TypeScript
*   **상태 관리**: Zustand
*   **빌드 도구**: Vite
*   **HTTP 클라이언트**: Axios (중앙 집중식 `httpClient` 사용)

## 📦 설치 및 실행

### 1. 저장소 클론

```bash
git clone <repository_url>
cd redis-kafka
```

### 2. Docker 설정 (Kafka, Redis, MySQL)

프로젝트는 Kafka, Redis, MySQL에 의존합니다. Docker Compose를 사용하여 쉽게 실행할 수 있습니다.

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

  mysql:
    image: mysql:8.0
    hostname: mysql
    container_name: mysql
    ports:
      - "3308:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root # Replace with a strong password in production
      MYSQL_DATABASE: test # Must match spring.datasource.url in application.properties
#      MYSQL_USER: your_username # Must match spring.datasource.username in application.properties
#      MYSQL_PASSWORD: your_password # Must match spring.datasource.password in application.properties
    volumes:
      - mysql_data:/var/lib/mysql # Persist data
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot_password"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  mysql_data:
```

그런 다음 Docker 컨테이너를 시작합니다:

```bash
docker-compose up -d
```

### 3. VAPID 키 생성

웹 푸시에는 VAPID 키 쌍이 필요합니다. 온라인 도구나 Node.js 스크립트를 사용하여 생성할 수 있습니다.

```javascript
// vapid-key-generator.js
const webpush = require('web-push');
const vapidKeys = webpush.generateVAPIDKeys();
console.log('Public Key:', vapidKeys.publicKey);
console.log('Private Key:', vapidKeys.privateKey);
```

**중요**: 개인 키는 매우 민감하므로 안전하게 보관하고 클라이언트 측에 노출하지 마십시오.

### 4. 백엔드 `application.properties` 구성

`web-push-with-redis-kafka-be/src/main/resources/application.properties` 파일을 열고 Kafka, Redis, Web Push VAPID 키 **및 MySQL 데이터베이스 설정**을 업데이트합니다:

```properties
# Kafka 설정
spring.kafka.bootstrap-servers=localhost:9092 # 로컬에서 백엔드 실행 시 Dockerized Kafka에 연결
spring.kafka.consumer.group-id=my-app-group
spring.kafka.consumer.auto-offset-reset=earliest

# Redis 설정
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Web Push VAPID 키
vapid.public.key=YOUR_VAPID_PUBLIC_KEY_HERE
vapid.private.key=YOUR_VAPID_PRIVATE_KEY_HERE

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3308/test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA and Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true # SQL 쿼리 로깅 시 가독성을 높이기 위해 포맷을 적용합니다.
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

**`YOUR_VAPID_PUBLIC_KEY_HERE`, `YOUR_VAPID_PRIVATE_KEY_HERE`, `your_database_name`, `your_username`, `your_password`**를 실제 값으로 대체하십시오.

### 5. 백엔드 애플리케이션 빌드 및 실행

`web-push-with-redis-kafka-be` 디렉토리로 이동하여 다음 명령을 실행합니다:

```bash
./gradlew bootRun
```

애플리케이션은 `http://localhost:8080`에서 시작됩니다.

### 6. 프론트엔드 애플리케이션 설치 및 실행

`web-push-with-redis-kafka-fe` 디렉토리로 이동하여 다음 명령을 실행합니다:

```bash
npm install
npm run dev
```

프론트엔드 애플리케이션은 `http://localhost:5173` (또는 Vite가 지정하는 다른 포트)에서 시작됩니다.

## 🎯 사용법

1.  **프론트엔드 구독**: 프론트엔드에서 알림 권한을 부여하고 푸시 알림을 구독합니다.
2.  **테스트 푸시 알림**: 프론트엔드 UI로 테스트 푸시 알림을 보냅니다. 백엔드가 Kafka로 메시지를 보내고, 소비되어 웹 푸시 알림으로 발송됩니다.
3.  **크롤링 시작**: 프론트엔드 UI로 크롤링 시뮬레이션을 시작합니다. 백엔드는 **STOMP over WebSocket**을 통해 **특정 사용자에게** 실시간 업데이트를 보내고 완료 시 최종 푸시 알림을 보냅니다.
4.  **크롤링 스케줄 관리**: 프론트엔드 UI의 스케줄 관리 섹션을 통해 Cron 표현식을 사용하여 크롤링 작업을 예약, 확인, 수정 또는 취소할 수 있습니다.

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

*   **실시간 크롤링 알림 흐름 (STOMP over WebSocket + Spring Session Redis)**:
    1.  **연결 수립**: 프론트엔드가 백엔드 인스턴스 중 하나와 `/ws` 엔드포인트로 STOMP over WebSocket 연결을 맺습니다.
    2.  **인증**: HTTP 핸드셰이크 시 JWT 토큰을 `Authorization` 헤더에 포함하여 전달하고, 백엔드의 `WebSocketAuthInterceptor`에서 이를 검증합니다.
    3.  **구독**: 클라이언트는 `/user/queue/crawling-progress` 목적지를 구독하여 자신에게 전송되는 크롤링 진행 상황 메시지를 수신합니다.
    4.  **크롤링 시작**: 프론트엔드가 `/api/crawling/start/{userId}`를 호출합니다.
    5.  **메시지 전송**: `CrawlingService`는 크롤링 작업을 비동기로 시작하고, 진행 상황 메시지를 `SimpMessagingTemplate.convertAndSendToUser(userId, "/queue/crawling-progress", message);`를 사용하여 특정 사용자에게 전송합니다.
    6.  **분산 처리**: `Spring Session Redis`가 활성화되어 있으므로, `SimpMessagingTemplate`이 보낸 메시지는 Redis를 통해 모든 백엔드 인스턴스로 전파되고, 해당 유저가 연결된 인스턴스에서 클라이언트로 메시지가 전달됩니다.

*   **크롤링 스케줄링 흐름 (Spring Batch + MySQL + 동적 스케줄러)**:
    1.  **스케줄 등록/관리**: 프론트엔드에서 새로운 크롤링 스케줄(사용자 ID, Cron 표현식)을 백엔드의 `/api/schedules/crawling` 엔드포인트로 전송합니다.
    2.  **서비스 처리**: `CrawlingScheduleController`가 요청을 받아 `CrawlingScheduleService`에 위임합니다.
    3.  **DB 저장 및 스케줄링**: `CrawlingScheduleService`는 스케줄 정보를 **MySQL 데이터베이스**의 `crawling_job_schedule` 테이블에 저장하고, {@link DynamicScheduler}를 통해 해당 Cron 표현식에 따라 작업을 예약합니다.
    4.  **배치 Job 실행**: 예약된 시각이 되면 {@link DynamicScheduler}는 Spring Batch의 {@link JobLauncher}를 사용하여 미리 정의된 `crawlingJob` ({@link CrawlingJobConfig}에 정의)을 실행합니다.
    5.  **크롤링 로직**: `crawlingJob` 내의 {@link Tasklet}은 {@link CrawlingService}를 호출하여 실제 크롤링 시뮬레이션 및 WebSocket을 통한 실시간 알림을 보냅니다.
    6.  **재시작 복구**: 애플리케이션 재시작 시, `CrawlingScheduleService`의 `@PostConstruct` 메서드는 MySQL에 저장된 모든 **SCHEDULED** 상태의 작업을 다시 로드하여 {@link DynamicScheduler}에 등록합니다.

## 2. 주요 파일 및 패키지

### 백엔드 (`com.mytoyappbe` 패키지)

*   `MyToyAppBeApplication.java`: Spring Boot 애플리케이션의 메인 진입점. `@EnableCaching` 어노테이션이 추가되어 Spring의 캐싱 메커니즘을 활성화합니다.

*   `com.mytoyappbe.common.config.RedisConfig.java`: Redis 연결과 `RedisTemplate`을 구성합니다. `GenericJackson2JsonRedisSerializer`를 사용하여 객체 직렬화를 처리합니다.
*   `com.mytoyappbe.websocket.WebSocketConfig.java`: STOMP over WebSocket 엔드포인트(`/ws`)를 등록하고 메시지 브로커를 구성합니다. `enableSimpleBroker`를 사용하여 인메모리 브로커를 활성화하고, `spring-session-data-redis`를 통해 Redis를 메시지 브로커의 백플레인으로 활용합니다.
*   `com.mytoyappbe.auth.config.security.WebSocketSecurityConfig.java`: STOMP WebSocket 연결 시 JWT 토큰을 검증하는 `WebSocketAuthInterceptor`를 등록하여 보안을 강화합니다.

*   `com.mytoyappbe.schedule.entity.Schedule.java`: 크롤링 스케줄 작업의 정보를 저장하는 JPA 엔티티입니다. Cron 표현식, 사용자 ID, 작업 이름, 상태 등 스케줄 관련 모든 정보를 포함합니다.

*   `com.mytoyappbe.schedule.repository.ScheduleRepository.java`: `CrawlingJobSchedule` 엔티티에 대한 데이터베이스 접근(CRUD)을 처리하는 Spring Data JPA 리포지토리입니다.

*   `com.mytoyappbe.batch.CrawlingJobConfig.java`: Spring Batch의 크롤링 작업을 정의합니다. 실제 크롤링 로직은 {@link CrawlingService}를 호출하는 {@link Tasklet} 내에서 실행됩니다.

*   `com.mytoyappbe.schedule.DynamicScheduler.java`: {@link ThreadPoolTaskScheduler}를 사용하여 Cron 표현식에 따라 Spring Batch 작업을 동적으로 스케줄링, 실행, 취소하는 핵심 컴포넌트입니다.

*   `com.mytoyappbe.websocket.handler.WebSocketConnectionHandler.java`: 일반 WebSocket 연결을 처리하는 핸들러 (현재 STOMP 기반 메시징에서는 직접 사용되지 않음).

*   `com.mytoyappbe.crawling.service.CrawlingService.java`: 모의 웹 크롤링 로직을 구현합니다. 크롤링 진행 상황 메시지를 `SimpMessagingTemplate.convertAndSendToUser()`를 사용하여 특정 사용자에게 전송합니다.
*   `com.mytoyappbe.notification.service.NotificationService.java`: 알림 메시지를 Kafka에 발행하는 역할을 합니다.
*   `com.mytoyappbe.webpush.service.WebPushService.java`: 웹 푸시 구독 정보를 Redis에 저장하고, 모든 구독자에게 푸시 알림을 전송합니다. `@Async` 어노테이션이 `sendNotificationToUser` 메서드에 추가되어 비동기적으로 알림을 전송하며, 오류 로깅이 개선되었습니다.
*   `com.mytoyappbe.schedule.service.ScheduleService.java`: 크롤링 작업 스케줄의 비즈니스 로직을 처리합니다. 데이터베이스에 스케줄 정보를 저장하고 {@link DynamicScheduler}를 통해 스케줄링 작업을 관리합니다. `getAllSchedules()` 및 `getScheduleById()` 메서드에 `@Cacheable` 어노테이션이 추가되어 Redis를 통한 캐싱을 활용합니다.

*   `com.mytoyappbe.notification.controller.NotificationController.java`: 알림 전송을 위한 REST 컨트롤러입니다. 알림 요청을 수신하는 엔드포인트 (`/api/notifications`)를 노출하고 `NotificationService`를 통해 Kafka에 게시합니다.
*   `com.mytoyappbe.webpush.controller.WebPushController.java`: 웹 푸시 구독 관리를 위한 REST 컨트롤러입니다. 프론트엔드로부터 `PushSubscriptionDto`를 수신하는 엔드포인트 (`/api/subscribe`)를 노출하고 `WebPushService`를 사용하여 이를 저장합니다.
*   `com.mytoyappbe.schedule.controller.CrawlingScheduleController.java`: 크롤링 작업 스케줄을 관리하기 위한 REST API를 제공합니다. 스케줄 추가, 조회, 수정, 취소 엔드포인트를 노출합니다.

*   `com.mytoyappbe.consumer`: Kafka 메시지를 소비하는 컨슈머를 포함합니다.
*   `com.mytoyappbe.dto`: 데이터 전송 객체(DTO)를 포함합니다.

### 프론트엔드 (React)

*   **`src/App.tsx`**: 메인 애플리케이션 컴포넌트. 초기 로직이 여러 컴포넌트와 훅으로 분리되었으며, 전역 `userId`를 Zustand 스토어에서 가져와 사용합니다.
*   **`src/components/SubscribeButton.tsx`**: `App.tsx`에서 분리된 푸시 알림 구독/해지 버튼 컴포넌트.
*   **`src/components/PushNotificationStatus.tsx`**: `App.tsx`에서 분리된 푸시 알림 상태 표시 컴포넌트.
*   **`src/components/CrawlingTable.tsx`**: STOMP over WebSocket 통신 및 크롤링 진행 상황을 표시하는 컴포넌트. `useCrawling` 훅과 `useWebSocketStore`를 활용하도록 리팩토링되었습니다.
*   **`src/hooks/usePushNotification.ts`**: 푸시 알림 관련 로직을 캡슐화하는 훅. 서버 구독 등록 로직을 포함하며 `userId`를 인자로 받습니다.
*   **`src/hooks/useServiceWorkerMessages.ts`**: 서비스 워커 메시지 처리를 담당하는 훅. `App.tsx`에서 분리되었습니다.
*   **`src/store/useWebSocketStore.ts`**: WebSocket 연결, 메시지 송수신, 재연결 로직을 캡슐화하는 Zustand 스토어. `userId`를 내부적으로 가져와 `wsUrl`을 구성합니다.
*   **`src/store/useAuthStore.ts`**: 전역 `userId` 상태를 관리하는 Zustand 스토어.
*   **`src/api/httpClient.ts`**: 중앙 집중식 Axios 인스턴스. 모든 API 모듈에서 재사용됩니다.
*   **`src/api/crawlingApi.ts`**: 크롤링 API 호출 모듈. `httpClient`를 사용하도록 업데이트되었습니다.
*   **`src/api/pushApi.ts`**: 푸시 알림 API 호출 모듈. `httpClient`를 사용하도록 업데이트되었습니다.
*   **`src/api/scheduleApi.ts`**: 크롤링 스케줄 API 호출 모듈. `httpClient`를 사용하도록 업데이트되었습니다.
