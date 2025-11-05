package com.mytoyappbe.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 토픽을 생성하고 관리하는 설정 클래스입니다.
 * 애플리케이션 시작 시 Spring에 의해 이 설정이 로드되며, 정의된 토픽이 Kafka 브로커에 없는 경우 자동으로 생성됩니다.
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * 푸시 알림 요청을 처리하기 위한 "notification-topic" 토픽을 생성하는 Bean입니다.
     * {@link TopicBuilder}를 사용하여 토픽의 이름, 파티션 수, 복제본 수를 설정합니다.
     *
     * @return {@link NewTopic} 객체를 반환하여 Spring이 Kafka에 토픽을 등록하도록 합니다.
     */
    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name("notification-topic")
                // partitions(1): 토픽의 파티션 수를 1로 설정합니다.
                // 파티션은 토픽을 구성하는 데이터의 하위 집합으로, 병렬 처리를 가능하게 합니다.
                // 컨슈머 그룹 내의 컨슈머 수만큼 파티션을 늘리면 처리량을 높일 수 있습니다.
                // 예를 들어, 3개의 컨슈머 인스턴스가 있다면 파티션을 3으로 설정하여 각 컨슈머가 다른 파티션을 처리하게 할 수 있습니다.
                .partitions(1)

                // replicas(1): 각 파티션의 복제본 수를 1로 설정합니다.
                // 복제본은 데이터의 고가용성(High Availability)과 내구성(Durability)을 보장하는 역할을 합니다.
                // replicas(3)으로 설정하면, 하나의 리더(Leader) 파티션과 두 개의 팔로워(Follower) 파티션이 생성됩니다.
                // 리더에 장애가 발생하면 팔로워 중 하나가 새로운 리더가 되어 서비스 중단을 방지합니다.
                // 운영 환경에서는 보통 2 또는 3 이상의 값을 권장합니다. 로컬 개발 환경이므로 1로 설정합니다.
                .replicas(1)

                // .compact(): 이 옵션을 사용하면 로그 압축(Log Compaction)을 활성화할 수 있습니다.
                // 로그 압축은 토픽의 각 메시지 키에 대해 최소 하나 이상의 최신 값만 보존하는 전략입니다.
                // 키-값 형태로 상태를 저장하는 토픽에 유용합니다. (예: 사용자의 마지막 로그인 위치)

                // .config(TopicConfig.RETENTION_MS_CONFIG, "-1"): 메시지 보존 기간을 설정합니다.
                // "-1"은 무한 보존을 의미합니다. 기본값은 브로커 설정을 따릅니다.
                .build();
    }
}
