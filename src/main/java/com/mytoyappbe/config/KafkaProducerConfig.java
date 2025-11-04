package com.mytoyappbe.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @class KafkaProducerConfig
 * @description Kafka 메시지 생산자를 설정하는 Spring 설정 클래스입니다.
 *              이 클래스는 Kafka 브로커에 메시지를 전송하는 데 필요한
 *              `ProducerFactory`와 `KafkaTemplate` 빈을 정의합니다.
 *
 * `@Configuration` 어노테이션은 이 클래스가 Spring의 설정 클래스임을 나타내며,
 * `@Bean` 어노테이션이 붙은 메서드들이 Spring 컨테이너에 의해 관리되는 빈을 생성함을 의미합니다.
 */
@Configuration
public class KafkaProducerConfig {

    // application.properties에서 Kafka 브로커 서버 주소를 주입받습니다.
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * @method producerFactory
     * @description Kafka 메시지 생산자를 생성하는 팩토리 빈을 정의합니다.
     *              이 팩토리는 Kafka 브로커 연결 정보, 메시지 직렬화 방식을 설정합니다.
     * @return `ProducerFactory<String, String>` - Kafka 메시지 생산자를 생성하는 팩토리
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        // Kafka 브로커 서버 주소를 설정합니다.
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 메시지 키를 직렬화할 클래스를 설정합니다. 여기서는 StringSerializer를 사용합니다.
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 메시지 값을 직렬화할 클래스를 설정합니다. 여기서는 StringSerializer를 사용합니다.
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * @method kafkaTemplate
     * @description Kafka 메시지를 전송하는 데 사용되는 `KafkaTemplate` 빈을 정의합니다.
     *              `KafkaTemplate`은 Kafka 생산자 API를 추상화하여 메시지 전송을 간소화합니다.
     *              이 빈을 통해 애플리케이션의 다른 컴포넌트에서 Kafka 메시지를 쉽게 발행할 수 있습니다.
     * @return `KafkaTemplate<String, String>` - Kafka 메시지 전송을 위한 템플릿
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}