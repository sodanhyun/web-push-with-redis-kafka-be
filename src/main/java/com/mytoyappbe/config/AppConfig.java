package com.mytoyappbe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 애플리케이션 전반에 걸쳐 사용될 공통 빈(Bean)들을 정의하는 Spring 설정 클래스입니다.
 * <p>
 * 주로 서드파티 라이브러리 객체나 복잡한 초기화가 필요한 객체들을 빈으로 등록하여,
 * 다른 컴포넌트에서 의존성 주입(Dependency Injection)을 통해 사용할 수 있도록 합니다.
 */
@Configuration
public class AppConfig {

    /**
     * JSON 직렬화(Serialization) 및 역직렬화(Deserialization)를 위한 {@link ObjectMapper} 빈을 생성합니다.
     * <p>
     * ObjectMapper는 Jackson 라이브러리의 핵심 클래스로, Java 객체와 JSON 데이터 간의 변환을 담당합니다.
     * 이 빈을 등록함으로써 애플리케이션의 다른 컴포넌트에서 {@code ObjectMapper}를 주입받아 사용할 수 있습니다.
     * <p>
     * <b>참고:</b> Spring Boot는 Jackson 라이브러리가 클래스패스에 존재할 경우,
     * 자동으로 {@code ObjectMapper} 빈을 생성하고 구성합니다.
     * 따라서 이 빈은 커스텀 설정(예: 날짜 포맷 변경, 특정 필드 무시)이 필요할 때 명시적으로 정의하는 것이 일반적입니다.
     * 현재는 기본 설정을 사용하지만, 향후 확장을 위해 빈으로 명시적으로 등록합니다.
     *
     * @return 기본 설정으로 초기화된 {@link ObjectMapper} 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}