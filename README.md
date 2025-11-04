# 🚀 웹 푸시 알림 백엔드 (Spring Boot)

이 프로젝트는 Spring Boot 백엔드 애플리케이션으로, 웹 푸시 알림 기능을 제공하고, Redis를 통한 구독 관리, Kafka를 통한 비동기 알림 처리, WebSocket을 통한 실시간 통신을 통합합니다.

## ✨ 주요 기능

*   **웹 푸시 프로토콜 구현**: VAPID를 사용하여 구독된 클라이언트에 푸시 알림을 보냅니다.
*   **Redis 통합**: 웹 푸시 구독 정보를 저장하고 관리합니다. (단순히 문자열 Set 형태로 저장)
*   **Kafka 통합**: 확장 가능하고 분리된 메시징을 위해 알림 요청을 비동기적으로 처리합니다.
*   **WebSocket 통신**: 웹 크롤링과 같은 프로세스에 대한 실시간 업데이트를 제공합니다.
*   **VAPID 키 관리**: 푸시 서비스 인증을 위해 VAPID 공개 및 개인 키를 안전하게 처리합니다.
*   **비동기 처리**: Spring의 `@Async`를 활용하여 비차단 작업을 수행하고 애플리케이션 응답성을 향상시킵니다.

## 🛠️ 기술 스택

*   **프레임워크**: Spring Boot 3.3.0
*   **언어**: Java 21
*   **빌드 도구**: Gradle
*   **메시징**: Apache Kafka
*   **데이터 저장소**: Redis
*   **Web Push 라이브러리**: `nl.martijndwars:web-push`
*   **실시간 통신**: Spring WebSocket
*   **유틸리티**: Lombok (상용구(boilerplate) 코드 감소)
*   **JSON 처리**: Jackson (자동 설정)

## 📦 설치 및 실행

이 프로젝트를 실행하려면 Java 21, Gradle, Docker (Kafka 및 Redis용), 그리고 VAPID 키 쌍이 설치되어 있어야 합니다.

### 1. 저장소 클론

```bash
git clone <repository_url>
cd web-push-with-redis-kafka-be
```

### 2. Docker 설정 (Kafka & Redis)

이 프로젝트는 Kafka와 Redis에 의존합니다. Docker Compose를 사용하여 쉽게 실행할 수 있습니다. 시스템에 Docker가 설치되어 실행 중인지 확인하십시오.

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
      - "9094:9094" # 호스트에서 접근 가능한 포트 (Spring Boot 앱이 호스트에서 실행될 경우)
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

### 3. VAPID 키 생성

웹 푸시에는 VAPID (Voluntary Application Server Identification) 키 쌍이 필요합니다. 다양한 온라인 도구나 라이브러리를 사용하여 생성할 수 있습니다. 예를 들어, Node.js 스크립트를 사용합니다 (Node.js/npm이 설치되어 있어야 합니다):

```javascript
// vapid-key-generator.js
const webpush = require('web-push');
const vapidKeys = webpush.generateVAPIDKeys();
console.log('Public Key:', vapidKeys.publicKey);
console.log('Private Key:', vapidKeys.privateKey);
```

위 스크립트를 실행한 후 출력되는 공개 키와 개인 키를 복사합니다.

**중요**: 개인 키는 매우 민감한 정보이므로 안전하게 보관하고 클라이언트 측에 **절대 노출하지 마십시오.**

### 4. `application.properties` 구성

`src/main/resources/application.properties` 파일을 열고 Kafka, Redis 및 VAPID 키 설정을 업데이트합니다:

```properties
# Kafka 설정
spring.kafka.bootstrap-servers=localhost:9094 # Kafka 브로커 주소 (Docker 내부 통신 시: kafka:29092)
spring.kafka.consumer.group-id=my-app-group # 컨슈머 그룹 ID
spring.kafka.consumer.auto-offset-reset=earliest # 컨슈머가 처음 실행될 때부터 메시지 읽기 시작

# Redis 설정
spring.data.redis.host=localhost # Redis 호스트 (Docker 내부 통신 시: redis)
spring.data.redis.port=6379

# Web Push VAPID 키
# 공개 키 (프론트엔드와 공유됨)
vapid.public.key=YOUR_VAPID_PUBLIC_KEY_HERE
# 개인 키 (백엔드에서만 사용되며 비밀로 유지되어야 함)
vapid.private.key=YOUR_VAPID_PRIVATE_KEY_HERE
```

**참고**:
*   `spring.kafka.bootstrap-servers`: Spring Boot 앱을 호스트 머신에서 실행하고 Kafka가 Docker에서 실행되는 경우 `localhost:9094`를 사용합니다. Spring Boot 앱도 Docker에서 실행되는 경우 `kafka:29092`를 사용합니다.
*   `spring.data.redis.host`: Spring Boot 앱을 호스트 머신에서 실행하고 Redis가 Docker에서 실행되는 경우 `localhost`를 사용합니다. Spring Boot 앱도 Docker에서 실행되는 경우 `redis`를 사용합니다.
*   `YOUR_VAPID_PUBLIC_KEY_HERE` 및 `YOUR_VAPID_PRIVATE_KEY_HERE`를 3단계에서 생성한 실제 VAPID 키로 대체하십시오.

### 5. 애플리케이션 빌드 및 실행

Gradle을 사용하여 Spring Boot 애플리케이션을 빌드하고 실행할 수 있습니다:

```bash
./gradlew bootRun
```

애플리케이션은 `http://localhost:8080`에서 시작됩니다.

## 🎯 사용법

백엔드가 실행 중이고 프론트엔드가 올바른 VAPID 공개 키와 API URL로 구성되면:

1.  **프론트엔드 구독**: 프론트엔드에서 알림 권한을 부여하고 푸시 알림을 구독합니다. 프론트엔드는 구독 세부 정보를 이 백엔드로 전송합니다.
2.  **테스트 푸시 알림**: 프론트엔드 UI를 사용하여 테스트 푸시 알림을 보냅니다. 이는 백엔드가 Kafka로 메시지를 보내도록 트리거하고, 메시지는 소비되어 웹 푸시 알림으로 발송됩니다.
3.  **크롤링 시작**: 프론트엔드 UI를 사용하여 크롤링 시뮬레이션을 시작합니다. 백엔드는 WebSocket을 통해 실시간 업데이트를 보내고 완료 시 최종 푸시 알림을 보냅니다.

---

# 📚 아키텍처 및 코드 설명

이 섹션에서는 애플리케이션의 아키텍처, 주요 파일 및 기능에 대한 개요를 제공합니다.

## 1. 전체 아키텍처

백엔드 애플리케이션은 Spring Boot의 종속성 주입, 비동기 처리, 그리고 Kafka 및 Redis와 같은 외부 서비스 통합 기능을 활용하는 모듈식 아키텍처로 설계되었습니다.

### 흐름 개요

*   **구독 흐름**:
    1.  프론트엔드는 `PushSubscription` 객체를 `/api/subscribe` 로 전송합니다.
    2.  `WebPushController`가 요청을 수신하고, `WebPushService`가 이 구독 정보를 **Redis**에 저장합니다.
*   **알림 발송 흐름**:
    1.  외부 요청 (예: 프론트엔드 테스트 버튼, Postman)이 `/api/notifications`로 알림 메시지를 보냅니다.
    2.  `NotificationController`가 요청을 수신하고, `NotificationService`가 메시지를 **Kafka**의 `notification-topic`으로 발행(publish)합니다.
    3.  `NotificationConsumer`가 Kafka에서 `notification-topic`의 메시지를 소비(consume)합니다.
    4.  `NotificationConsumer`는 `WebPushService`에 실제 푸시 알림을 보내도록 위임합니다.
    5.  `WebPushService`는 **Redis**에 저장된 모든 `PushSubscription` 데이터를 검색합니다.
    6.  `WebPushService`는 VAPID 키를 사용하여 푸시 요청에 서명하고 외부 푸시 서비스 (예: Google FCM)로 전송합니다.
    7.  푸시 서비스는 사용자 브라우저에 최종 알림을 전달합니다.
*   **WebSocket 테스트 흐름**:
    1.  프론트엔드는 `/ws/test/{userId}`로 WebSocket 연결을 시도합니다.
    2.  `TestWebSocketHandler`는 연결을 설정하고, 1초 간격으로 10개의 메시지를 클라이언트에 보냅니다.
    3.  프론트엔드는 메시지를 수신하고 표시합니다.
*   **크롤링 및 알림 흐름**:
    1.  프론트엔드는 `/api/crawling/start`로 크롤링 시작 요청을 보냅니다.
    2.  `CrawlingController`는 `CrawlingService.startCrawling` 메서드를 비동기적으로 (`@Async`) 호출합니다.
    3.  `CrawlingService`는 데이터 생성을 시뮬레이션하고, `WebSocketSessionManager`를 통해 연결된 클라이언트에 실시간 업데이트를 보냅니다.
    4.  시뮬레이션 완료 후, `CrawlingService`는 `WebPushService`를 호출하여 모든 구독자에게 "크롤링 완료" 푸시 알림을 보냅니다.

## 2. 주요 파일 및 패키지

### `com.mytoyappbe` 패키지

*   `MyToyAppBeApplication.java`: 메인 Spring Boot 애플리케이션 클래스입니다. `@SpringBootApplication` 어노테이션을 통해 자동 구성 및 컴포넌트 스캔을 활성화하고, `@EnableAsync` 어노테이션을 통해 비동기 메서드 실행을 활성화합니다.

### `com.mytoyappbe.config` 패키지

*   `AppConfig.java`: JSON 직렬화/역직렬화를 위한 `ObjectMapper`와 같은 애플리케이션 전반에 걸쳐 사용되는 공통 빈(Bean)들을 정의합니다.
*   `KafkaConsumerConfig.java`: Kafka 소비자를 구성합니다. 부트스트랩 서버, 그룹 ID, 메시지 키 및 값의 역직렬화 설정 등을 포함합니다.
*   `KafkaProducerConfig.java`: Kafka 생산자를 구성합니다. 부트스트랩 서버, 메시지 키 및 값의 직렬화 설정 등을 포함하며, `KafkaTemplate` 빈을 제공하여 Kafka 메시지 발행을 쉽게 합니다.
*   `KafkaTopicConfig.java`: Kafka 토픽(예: `notification-topic`)을 Spring 빈으로 정의합니다. 애플리케이션 시작 시 해당 토픽이 Kafka에 자동으로 생성되도록 합니다.
*   `RedisConfig.java`: Redis 연결 팩토리를 구성하고, Redis와 상호 작용하기 위한 `StringRedisTemplate` 빈을 제공합니다.
*   `WebPushConfig.java`: `web-push` 라이브러리의 `PushService` 빈을 구성합니다. `application.properties`에서 VAPID 공개 및 개인 키를 로드하여 `PushService`를 초기화하며, 이는 푸시 요청에 대한 VAPID 서명을 처리합니다.
*   `WebSocketConfig.java`: WebSocket 엔드포인트를 구성합니다. `@EnableWebSocket`을 통해 WebSocket 지원을 활성화하고, `/ws/test` 경로에 `TestWebSocketHandler`를 등록하여 모든 출처의 연결을 허용합니다.

### `com.mytoyappbe.consumer` 패키지

*   `NotificationConsumer.java`: `notification-topic`을 리스닝하는 Kafka 소비자입니다. 메시지가 수신되면 `WebPushService`에 위임하여 구독된 모든 클라이언트에 실제 푸시 알림을 보냅니다.

### `com.mytoyappbe.controller` 패키지

*   `CrawlingController.java`: 웹 크롤링 시작을 위한 REST 컨트롤러입니다. `CrawlingService`를 트리거하는 엔드포인트 (`/api/crawling/start/{userId}`)를 노출합니다.
*   `NotificationController.java`: 알림 전송을 위한 REST 컨트롤러입니다. 알림 요청을 수신하는 엔드포인트 (`/api/notifications`)를 노출하고 `NotificationService`를 통해 Kafka에 게시합니다.
*   `WebPushController.java`: 웹 푸시 구독 관리를 위한 REST 컨트롤러입니다. 프론트엔드로부터 `PushSubscriptionDto`를 수신하는 엔드포인트 (`/api/subscribe`)를 노출하고 `WebPushService`를 사용하여 이를 저장합니다.

### `com.mytoyappbe.dto` 패키지

*   `KafkaNotificationMessageDto.java`: Kafka의 `notification-topic`으로 전송/수신되는 메시지를 위한 DTO(Data Transfer Object)입니다.
*   `PushSubscriptionDto.java`: 프론트엔드로부터 수신되는 웹 푸시 구독 객체를 나타내는 DTO입니다. `endpoint`와 `keys` (p256dh, auth)를 포함합니다.

### `com.mytoyappbe.handler` 패키지

*   `TestWebSocketHandler.java`: `/ws/test` 엔드포인트에 대한 WebSocket 연결을 처리하는 핸들러입니다. `TextWebSocketHandler`를 상속하며, 테스트 목적으로 연결된 클라이언트에 예약된 메시지를 전송하는 로직을 관리합니다.

### `com.mytoyappbe.manager` 패키지

*   `WebSocketSessionManager.java`: 활성 WebSocket 세션을 관리합니다. WebSocket 세션을 추가, 제거 및 검색하는 메서드를 제공하여 서비스 (`CrawlingService` 등)가 특정 클라이언트에 메시지를 보낼 수 있도록 합니다.

### `com.mytoyappbe.service` 패키지

*   `CrawlingService.java`: 모의 웹 크롤링 로직을 구현합니다. `@Async` 서비스로, 데이터 생성을 시뮬레이션하고, `WebSocketSessionManager`를 통해 실시간 업데이트를 전송하며, 시뮬레이션 완료 시 `WebPushService`를 통해 "크롤링 완료" 푸시 알림을 발송합니다.
*   `NotificationService.java`: 알림 메시지를 Kafka에 발행(publish)하는 역할을 담당합니다. `KafkaTemplate`을 사용하여 `KafkaNotificationMessageDto`를 `notification-topic`에 전송합니다.
*   `WebPushService.java`: 웹 푸시 알림의 핵심 서비스입니다.
    *   `saveSubscription()`: `PushSubscriptionDto`를 JSON 문자열로 변환하고 Redis (중복 방지를 위해 `Set` 자료구조 사용)에 저장합니다.
    *   `sendNotificationToAll()`: Redis에서 모든 구독 정보를 검색하고, `Notification` 객체를 구성한 후 `PushService`를 사용하여 외부 푸시 서비스로 푸시 메시지를 전송합니다.

## 3. 사용된 기술

*   **Spring Boot**: 프로덕션 준비가 된 Spring 애플리케이션 개발을 단순화하는 프레임워크입니다.
*   **Java 21**: 백엔드에 사용된 프로그래밍 언어입니다.
*   **Gradle**: 종속성 관리 및 프로젝트 빌드에 사용되는 강력한 빌드 자동화 도구입니다.
*   **Apache Kafka**: 실시간 데이터 파이프라인 및 스트리밍 애플리케이션 구축에 사용되는 분산 스트리밍 플랫폼입니다. 여기서는 비동기 알림 처리에 사용됩니다.
*   **Redis**: 오픈 소스, 인메모리 데이터 구조 저장소로, 데이터베이스, 캐시 및 메시지 브로커로 사용됩니다. 여기서는 웹 푸시 구독 데이터를 저장합니다.
*   **`nl.martijndwars:web-push`**: 웹 푸시 알림 전송을 위한 Java 라이브러리입니다. VAPID 서명 및 푸시 서비스로의 HTTP/2 요청의 복잡성을 처리합니다.
*   **Spring WebSocket**: Spring 애플리케이션에서 WebSocket 통신을 지원하여 클라이언트와 서버 간의 전이중 통신을 가능하게 합니다.
*   **Lombok**: Java 클래스를 "향상"시키는 Java 라이브러리로, getter, setter, 생성자 등을 자동 생성하여 상용구 코드를 줄입니다.
*   **Jackson**: Java 객체와 JSON 데이터 간의 효율적인 직렬화 및 역직렬화를 위한 라이브러리입니다.
*   **VAPID (Voluntary Application Server Identification)**: 푸시 서비스에 애플리케이션 서버를 식별하기 위한 표준입니다. 공개 및 개인 키 쌍을 사용하여 승인된 서버만 푸시 메시지를 보낼 수 있도록 합니다.
*   **Docker & Docker Compose**: 로컬 개발 환경에서 Kafka 및 Redis 인스턴스를 쉽게 설정하고 관리하는 데 사용됩니다.
