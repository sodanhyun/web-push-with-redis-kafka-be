# `my-toy-app-be` 프로젝트 코드 설명서

## 1. 개요

이 문서는 Spring Boot를 기반으로 구축된 웹 푸시 알림 백엔드 서버 `my-toy-app-be`의 전체 코드 구조, 각 파일의 역할, 그리고 핵심 로직의 작동 원리를 설명합니다.

이 애플리케이션은 다음과 같은 주요 기술 스택을 사용합니다.

*   **프레임워크:** Spring Boot
*   **언어:** Java 21
*   **빌드 도구:** Gradle
*   **핵심 기능:**
    *   Web Push Protocol을 이용한 푸시 알림 발송
    *   Redis를 이용한 웹 푸시 구독 정보 저장
    *   Kafka를 이용한 알림 발송 요청 비동기 처리

## 2. 전체 아키텍처 및 흐름

애플리케이션은 크게 **"구독(Subscription)"**과 **"알림 발송(Notification)"** 두 가지 흐름으로 동작합니다.

### 구독 흐름

1.  **[프론트엔드]** 사용자가 브라우저에서 "알림 받기"를 클릭합니다.
2.  **[프론트엔드]** 브라우저는 Push Service(Google, Apple 등)로부터 `PushSubscription` 객체를 발급받습니다.
3.  **[프론트엔드]** 이 `PushSubscription` 객체를 백엔드의 `/api/subscribe` 엔드포인트로 전송합니다.
4.  **[백엔드]** `WebPushController`가 요청을 받아 `WebPushService`에게 전달합니다.
5.  **[백엔드]** `WebPushService`는 받은 `PushSubscription` 객체를 JSON 문자열 형태로 **Redis**에 저장합니다.

### 알림 발송 흐름

1.  **[외부 요청]** Postman 또는 다른 서비스에서 알림 메시지를 담아 `/api/notifications` 엔드포인트로 POST 요청을 보냅니다.
2.  **[백엔드]** `NotificationController`가 요청을 받아 `NotificationService`에게 메시지를 전달합니다.
3.  **[백엔드]** `NotificationService`는 이 메시지를 **Kafka**의 `notification` 토픽으로 발행(Produce)합니다. (느슨한 결합)
4.  **[백엔드]** `NotificationConsumer`가 `notification` 토픽을 구독(Consume)하고 있다가 메시지를 수신합니다.
5.  **[백엔드]** `NotificationConsumer`는 `WebPushService`를 호출하여 실제 푸시 알림 발송을 위임합니다.
6.  **[백엔드]** `WebPushService`는 **Redis**에 저장된 모든 `PushSubscription` 정보를 가져옵니다.
7.  **[백엔드]** 각각의 구독 정보에 대해, `application.properties`에 저장된 VAPID 키로 요청을 서명하여 외부 Push Service(Google 등)로 푸시 알림 전송을 요청합니다.
8.  **[Push Service]** 해당 사용자에게 푸시 알림을 최종적으로 전달합니다.

## 3. 주요 파일 및 코드 설명

### 3.1. 빌드 및 설정

#### `build.gradle`

프로젝트의 의존성, 플러그인, 버전을 관리하는 빌드 스크립트입니다.

```groovy
plugins {
    id 'java' // 자바 플러그인 적용
    id 'org.springframework.boot' version '3.3.0' // 스프링 부트 플러그인 (안정 버전으로 수정됨)
    id 'io.spring.dependency-management' version '1.1.5' // 의존성 관리 플러그인
}

group = 'com'
version = '0.0.1-SNAPSHOT'
description = 'my-toy-app-be'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // Java 21 사용
    }
}

repositories {
    mavenCentral() // 의존성을 다운로드할 중앙 저장소
}

dependencies {
    // Spring Boot 기본 의존성
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'

    // Redis
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // Web Push 라이브러리
    implementation 'nl.martijndwars:web-push:5.1.1'
    // web-push 라이브러리가 내부적으로 사용하는 HTTP 클라이언트
    implementation 'org.apache.httpcomponents:httpclient'
    // web-push 라이브러리가 사용하는 암호화 관련 의존성
    implementation 'org.bouncycastle:bcprov-jdk18on:1.78.1'

    // Lombok (코드 다이어트용)
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // 테스트용 의존성
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
// ...
```

*   **`org.springframework.boot`**: 실행 가능한 Jar 파일을 만들고 의존성 버전을 관리하는 등 스프링 부트의 핵심 기능을 제공합니다. `3.3.0`으로 버전을 명시하여 안정성을 확보했습니다.
*   **`spring-kafka`**: 스프링 애플리케이션에서 Kafka를 쉽게 사용하도록 돕는 라이브러리입니다.
*   **`spring-boot-starter-data-redis`**: Redis와의 연동을 자동 설정하고 `RedisTemplate` 등을 제공합니다.
*   **`nl.martijndwars:web-push`**: Web Push Protocol을 Java로 구현한 라이브러리로, VAPID 서명 및 푸시 요청 전송을 담당합니다.

#### `application.properties`

애플리케이션의 주요 설정을 담당하는 파일입니다.

```properties
# Kafka
# 카프카 브로커(서버)의 주소
spring.kafka.bootstrap-servers=localhost:9092
# 컨슈머 그룹 ID
spring.kafka.consumer.group-id=my-group
# 컨슈머가 처음 실행될 때, 읽지 않은 모든 메시지를 처음부터 읽도록 설정
spring.kafka.consumer.auto-offset-reset=earliest

# Redis
# 레디스 서버의 주소
spring.data.redis.host=localhost
# 레디스 서버의 포트
spring.data.redis.port=6379

# Web Push VAPID Keys
# VAPID 공개키 (프론트엔드로 전달되어 구독 시 사용됨)
vapid.public.key=BGoBrUi2hGztU7JK-gMPqjej-Ij_xl_81bew8Ij2VppesKZoIz8z7gjQaOQSvgV89UUXL_e7WO8arB_vXDPxTUs
# VAPID 개인키 (서버에서 푸시 요청을 서명할 때 사용됨)
vapid.private.key=Hock8VEnkPj25OHTZnqaFd0ybSEyD1pAepFP9ouVthI
```

*   **`spring.kafka.*`**: Kafka 서버 접속 정보와 Consumer 동작 방식을 정의합니다.
*   **`spring.data.redis.*`**: Redis 서버 접속 정보를 정의합니다.
*   **`vapid.*.key`**: Web Push의 핵심 인증 정보입니다. 서버와 프론트엔드는 **반드시 짝이 맞는 키**를 사용해야 합니다.

### 3.2. Java 소스 코드

#### `config` 패키지: 각종 설정 클래스

*   **`RedisConfig.java`**:
    *   **목적**: Redis와의 통신을 담당할 `RedisTemplate`을 설정하고 Spring Bean으로 등록합니다.
    *   **원리**: `RedisConnectionFactory`를 주입받아 `StringRedisTemplate`을 생성합니다. Key와 Value를 모두 문자열로 직렬화(Serialize)하여 Redis에 저장하고 읽을 수 있게 해줍니다.

*   **`Kafka*Config.java`**:
    *   **목적**: Kafka의 Producer, Consumer, Topic을 설정합니다.
    *   `KafkaProducerConfig`: 메시지를 Kafka로 보낼 Producer를 설정합니다. 메시지의 Key와 Value를 문자열로 직렬화하도록 지정합니다.
    *   `KafkaConsumerConfig`: 메시지를 Kafka에서 받을 Consumer를 설정합니다. 그룹 ID, 역직렬화 방식 등을 정의합니다.
    *   `KafkaTopicConfig`: `notification`이라는 이름의 새로운 Topic을 생성하는 `NewTopic` Bean을 정의합니다. 애플리케이션 시작 시 해당 토픽이 없으면 자동으로 생성됩니다.

*   **`WebPushConfig.java`**:
    *   **목적**: `web-push` 라이브러리의 핵심인 `PushService`를 설정하고 Spring Bean으로 등록합니다.
    *   **원리**: `@ConfigurationProperties`를 이용해 `application.properties`에 정의된 `vapid.public.key`와 `vapid.private.key` 값을 읽어옵니다. 이 키들을 사용하여 `PushService` 객체를 초기화합니다. 이렇게 생성된 `PushService`는 푸시 알림을 보낼 때마다 VAPID 서명을 자동으로 처리합니다.

#### `dto` 패키지: 데이터 전송 객체

*   **`PushSubscriptionDto.java`**:
    *   **목적**: 프론트엔드에서 전달하는 `PushSubscription` JSON 객체를 Java 객체로 변환하기 위한 클래스입니다.
    *   **원리**: `endpoint`, `keys` 등 브라우저가 발급한 구독 정보의 구조와 정확히 일치하는 필드를 가지고 있습니다. `keys` 필드는 내부 클래스 `Keys`로 한 번 더 매핑됩니다.

#### `controller` 패키지: API 엔드포인트

*   **`WebPushController.java`**:
    *   **목적**: 프론트엔드로부터 구독 요청을 받기 위한 API 엔드포인트를 제공합니다.
    *   **원리**: `/api/subscribe` 경로로 POST 요청이 오면, 요청 본문(body)에 담긴 `PushSubscriptionDto`를 받아 `WebPushService`의 `saveSubscription` 메소드를 호출하여 구독 정보를 저장합니다.

*   **`NotificationController.java`**:
    *   **목적**: 외부로부터 알림 발송 요청을 받기 위한 API 엔드포인트를 제공합니다.
    *   **원리**: `/api/notifications` 경로로 POST 요청이 오면, 본문에 담긴 메시지(문자열)를 받아 `NotificationService`의 `sendNotification` 메소드를 호출합니다.

#### `service` 패키지: 핵심 비즈니스 로직

*   **`WebPushService.java`**:
    *   **목적**: 웹 푸시 구독 정보를 Redis에 저장하고, 저장된 모든 구독자에게 푸시 알림을 발송하는 핵심 로직을 담당합니다.
    *   **`saveSubscription`**: `PushSubscriptionDto`를 JSON 문자열로 변환하여 Redis의 `Set` 자료구조에 저장합니다. `Set`을 사용하면 중복된 구독 정보가 저장되지 않습니다.
    *   **`sendNotificationToAll`**: Redis에 저장된 모든 구독 정보(JSON 문자열)를 가져옵니다. 각 문자열을 다시 `PushSubscriptionDto` 객체로 변환한 후, `web-push` 라이브러리의 `Subscription`과 `Notification` 객체를 만듭니다. 마지막으로 `pushService.send()`를 호출하여 외부 Push Service로 발송 요청을 보냅니다.

*   **`NotificationService.java`**:
    *   **목적**: 알림 발송 요청을 받아 Kafka 토픽으로 메시지를 보내는 역할을 합니다.
    *   **원리**: `KafkaTemplate`을 사용하여 `notification` 토픽으로 메시지를 발행(produce)합니다. 이는 알림 발송 과정을 비동기적으로 처리하고, `NotificationController`와 실제 발송 로직(`WebPushService`) 간의 의존성을 낮추는 역할을 합니다.

#### `consumer` 패키지: Kafka 메시지 수신

*   **`NotificationConsumer.java`**:
    *   **목적**: Kafka의 `notification` 토픽을 구독하여 메시지를 수신하고, 실제 푸시 알림 발송을 트리거합니다.
    *   **원리**: `@KafkaListener` 어노테이션을 사용하여 `notification` 토픽에 새로운 메시지가 들어오면 `listen` 메소드가 자동으로 호출됩니다. 이 메소드는 수신한 메시지를 `WebPushService`의 `sendNotificationToAll` 메소드로 전달하여 모든 구독자에게 알림을 발송하도록 지시합니다.

---
이 문서가 프로젝트의 구조와 동작을 이해하는 데 도움이 되기를 바랍니다.
