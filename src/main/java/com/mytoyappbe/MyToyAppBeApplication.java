package com.mytoyappbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @class MyToyAppBeApplication
 * @description Spring Boot 애플리케이션의 메인 진입점 클래스입니다.
 *              이 클래스는 애플리케이션을 부트스트랩하고 실행하는 역할을 합니다.
 *
 * 주요 어노테이션:
 * - `@SpringBootApplication`: Spring Boot 애플리케이션의 핵심 어노테이션으로, 다음 세 가지를 포함합니다.
 *   - `@Configuration`: 애플리케이션 컨텍스트에 빈 정의 소스를 제공합니다.
 *   - `@EnableAutoConfiguration`: 클래스패스 설정 및 기타 빈을 기반으로 Spring Boot가 자동으로 빈을 추가하도록 지시합니다.
 *   - `@ComponentScan`: `com.mytoyappbe` 패키지 및 하위 패키지에서 컴포넌트, 서비스, 리포지토리 등을 찾아 빈으로 등록합니다.
 * - `@EnableAsync`: Spring의 비동기 메서드 실행 기능을 활성화합니다.
 *   이 어노테이션이 붙은 메서드는 별도의 스레드에서 실행되어 호출 스레드를 블록하지 않습니다.
 *   (예: `@Async` 어노테이션이 붙은 서비스 메서드)
 */
@SpringBootApplication
@EnableAsync
public class MyToyAppBeApplication {

    /**
     * @method main
     * @description Java 애플리케이션의 메인 메서드입니다.
     *              이 메서드를 통해 Spring Boot 애플리케이션이 시작됩니다.
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        // SpringApplication.run() 메서드는 애플리케이션을 부트스트랩하고
        // 내장된 Tomcat 서버를 시작하여 애플리케이션을 실행합니다.
        SpringApplication.run(MyToyAppBeApplication.class, args);
    }

}