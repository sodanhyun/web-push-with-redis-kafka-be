package com.mytoyappbe.service.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Pub/Sub 메시지 발행을 담당하는 서비스 클래스입니다.
 * 특정 토픽으로 메시지를 발행하여 구독자들에게 전달합니다.
 */
@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 지정된 토픽으로 메시지를 발행합니다.
     * RedisTemplate의 convertAndSend 메서드를 사용하여 메시지를 직렬화하고 발행합니다.
     *
     * @param topic 메시지를 발행할 Redis 토픽 채널 (예: "ws:user:userId")
     * @param message 발행할 메시지 객체. RedisTemplate에 의해 자동으로 직렬화됩니다.
     */
    public void publish(String topic, Object message) {
        redisTemplate.convertAndSend(topic, message);
    }
}
