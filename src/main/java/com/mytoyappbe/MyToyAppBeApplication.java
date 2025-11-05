package com.mytoyappbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot 애플리케이션의 메인 진입점 클래스입니다.
 * <p>
 * 이 클래스는 애플리케이션을 부트스트랩하고 실행하는 역할을 하며,
 * 주요 설정 및 컴포넌트 스캔을 담당합니다.
 */
@SpringBootApplication // Spring Boot 애플리케이션의 핵심 어노테이션
@EnableAsync // Spring의 비동기 메서드 실행 기능을 활성화합니다.
public class MyToyAppBeApplication {

    /**
     * Java 애플리케이션의 메인 메서드입니다。
     * <p>
     * 이 메서드를 통해 Spring Boot 애플리케이션이 시작됩니다.
     * {@link SpringApplication#run(Class, String...)} 메서드는 다음을 수행합니다:
     * <ul>
     *     <li>Spring 애플리케이션 컨텍스트를 생성하고 초기화합니다.</li>
     *     <li>클래스패스에 있는 모든 빈 정의를 로드합니다.</li>
     *     <li>내장된 웹 서버(기본적으로 Tomcat)를 시작하여 애플리케이션을 배포합니다.</li>
     *     <li>{@code @SpringBootApplication}이 활성화하는 자동 설정 및 컴포넌트 스캔을 수행합니다.</li>
     * </ul>
     *
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(MyToyAppBeApplication.class, args);
    }

}