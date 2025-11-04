package com.mytoyappbe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @class AppConfig
 * @description 애플리케이션 전반에 걸쳐 사용될 공통 빈(Bean)들을 정의하는 Spring 설정 클래스입니다.
 *              주로 서드파티 라이브러리 객체나 복잡한 초기화가 필요한 객체들을 빈으로 등록합니다.
 *
 * `@Configuration` 어노테이션은 이 클래스가 Spring의 설정 클래스임을 나타내며,
 * `@Bean` 어노테이션이 붙은 메서드들이 Spring 컨테이너에 의해 관리되는 빈을 생성함을 의미합니다.
 */
@Configuration
public class AppConfig {

    /**
     * @method objectMapper
     * @description JSON 직렬화 및 역직렬화를 위한 `ObjectMapper` 빈을 제공합니다.
     *              `ObjectMapper`는 Jackson 라이브러리의 핵심 클래스로, Java 객체와 JSON 데이터 간의 변환을 처리합니다.
     *              이 빈을 등록함으로써 애플리케이션의 다른 컴포넌트에서 `ObjectMapper`를 주입받아 사용할 수 있습니다.
     *              (예: REST 컨트롤러에서 요청 본문을 객체로 변환하거나, 객체를 응답 본문으로 변환할 때)
     * @return `ObjectMapper` - 새로 생성된 `ObjectMapper` 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}