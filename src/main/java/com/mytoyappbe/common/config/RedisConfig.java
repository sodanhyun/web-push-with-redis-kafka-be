package com.mytoyappbe.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper 임포트
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator; // BasicPolymorphicTypeValidator 임포트
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator; // PolymorphicTypeValidator 임포트
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 관련 설정을 담당하는 Spring 설정 클래스입니다.
 * <p>
 * Redis 서버와의 연결, 데이터 처리를 위한 {@link RedisTemplate},
 * 그리고 Pub/Sub 메시지 수신을 위한 {@link RedisMessageListenerContainer} 빈을 정의합니다.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * Redis 서버와의 연결을 생성하는 {@link RedisConnectionFactory} 빈을 정의합니다.
     * <p>
     * Spring Data Redis는 Redis 클라이언트 라이브러리로 Lettuce와 Jedis를 지원합니다.
     * 여기서는 비동기 및 리액티브 프로그래밍을 지원하는 고성능 클라이언트인 **Lettuce**를 사용합니다.
     *
     * @return Lettuce 기반의 {@link RedisConnectionFactory} 인스턴스
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * Redis 데이터 작업을 위한 고수준 추상화 클래스인 {@link RedisTemplate} 빈을 정의합니다.
     * 이 템플릿은 Redis의 다양한 데이터 구조(String, Set, List, Hash, ZSet)에 대한 편리한 메서드를 제공합니다.
     *
     * @return 직렬화 설정이 완료된 {@link RedisTemplate} 인스턴스
     */
    @Bean
    @Primary // 이 RedisTemplate 빈을 우선적으로 사용하도록 지정
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // Key Serializer: Redis 키를 문자열로 직렬화합니다.
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Value Serializer: Redis 값을 JSON 형식으로 직렬화합니다.
        // GenericJackson2JsonRedisSerializer는 Java 객체를 JSON으로 변환하여 저장하므로,
        // 다양한 타입의 객체를 유연하게 저장하고 역직렬화할 수 있습니다.
        // ObjectMapper를 커스터마이징하여 다형성 직렬화를 활성화합니다.
        ObjectMapper objectMapper = new ObjectMapper();
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class) // 모든 Object 타입에 대해 다형성 허용
                .build();
        // DefaultTyping.EVERYTHING 대신 DefaultTyping.NON_FINAL을 사용하고, As.PROPERTY로 타입 정보를 속성으로 포함
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        // Hash Key Serializer: 해시 데이터 구조의 필드(키)를 문자열로 직렬화합니다.
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Hash Value Serializer: 해시 데이터 구조의 값(value)을 JSON 형식으로 직렬화합니다.
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)); // 동일한 ObjectMapper 사용

        return redisTemplate;
    }

    /**
     * Redis Pub/Sub 메시지를 수신하는 리스너 컨테이너 빈을 정의합니다.
     * 이 컨테이너는 특정 토픽(채널)에 대한 메시지 리스너를 등록하고, 메시지 수신을 관리합니다.
     *
     * @param connectionFactory Redis 연결을 위한 팩토리
     * @return 설정이 완료된 {@link RedisMessageListenerContainer} 인스턴스
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        return container;
    }
}
