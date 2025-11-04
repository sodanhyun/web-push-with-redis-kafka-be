package com.mytoyappbe.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @class KafkaConsumerConfig
 * @description Kafka 메시지 소비자를 설정하는 Spring 설정 클래스입니다.
 *              이 클래스는 Kafka 브로커에 연결하고 메시지를 소비하는 데 필요한
 *              `ConsumerFactory`와 `ConcurrentKafkaListenerContainerFactory` 빈을 정의합니다.
 *
 * `@Configuration` 어노테이션은 이 클래스가 Spring의 설정 클래스임을 나타내며,
 * `@Bean` 어노테이션이 붙은 메서드들이 Spring 컨테이너에 의해 관리되는 빈을 생성함을 의미합니다.
 */
@Configuration
public class KafkaConsumerConfig {

    // application.properties에서 Kafka 브로커 서버 주소를 주입받습니다.
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // application.properties에서 Kafka 소비자 그룹 ID를 주입받습니다.
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * @method consumerFactory
     * @description Kafka 메시지 소비자를 생성하는 팩토리 빈을 정의합니다.
     *              이 팩토리는 Kafka 브로커 연결 정보, 그룹 ID, 메시지 역직렬화 방식을 설정합니다.
     * @return `ConsumerFactory<String, String>` - Kafka 메시지 소비자를 생성하는 팩토리
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Kafka 브로커 서버 주소를 설정합니다.
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 소비자가 속할 그룹 ID를 설정합니다.
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // 메시지 키를 역직렬화할 클래스를 설정합니다. 여기서는 StringDeserializer를 사용합니다.
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 메시지 값을 역직렬화할 클래스를 설정합니다. 여기서는 StringDeserializer를 사용합니다.
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * @method kafkaListenerContainerFactory
     * @description Kafka 리스너 컨테이너 팩토리 빈을 정의합니다.
     *              이 팩토리는 `@KafkaListener` 어노테이션이 붙은 메서드들을 처리하는 데 사용됩니다.
     *              `consumerFactory`를 사용하여 Kafka 소비자를 생성하고 관리합니다.
     * @return `ConcurrentKafkaListenerContainerFactory<String, String>` - Kafka 리스너 컨테이너 팩토리
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 위에서 정의한 consumerFactory를 설정하여 Kafka 소비자를 생성하도록 합니다.
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}