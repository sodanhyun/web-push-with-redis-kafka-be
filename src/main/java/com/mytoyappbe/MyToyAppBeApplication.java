/**
 * @file MyToyAppBeApplication.java
 * @description Spring Boot 애플리케이션의 메인 진입점입니다.
 *              애플리케이션 시작 시 비동기 기능을 활성화하고,
 *              `CommandLineRunner`를 구현하여 테스트 사용자 계정을 초기화합니다.
 */

package com.mytoyappbe;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mytoyappbe.auth.domain.User;
import com.mytoyappbe.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;

/**
 * @class MyToyAppBeApplication
 * @description Spring Boot 애플리케이션의 메인 클래스입니다.
 *              `@EnableAsync`를 통해 비동기 메서드 실행을 활성화하고,
 *              `@SpringBootApplication`으로 Spring Boot의 자동 설정을 사용합니다.
 *              `CommandLineRunner`를 구현하여 애플리케이션 시작 시 초기화 작업을 수행합니다.
 */
@EnableAsync // Spring의 비동기 메서드(@Async) 기능을 활성화합니다.
@SpringBootApplication // Spring Boot 애플리케이션의 시작점을 나타내며, 자동 설정, 컴포넌트 스캔 등을 활성화합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다. (의존성 주입)
public class MyToyAppBeApplication implements CommandLineRunner {
	private final UserRepository userRepository; // 사용자 데이터베이스 접근을 위한 리포지토리
	private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 인코더

	/**
	 * @method main
	 * @description Spring Boot 애플리케이션의 메인 메서드입니다.
	 *              애플리케이션을 시작하는 역할을 합니다.
	 * @param {String[]} args - 커맨드 라인 인자
	 */
	public static void main(String[] args) {
		SpringApplication.run(MyToyAppBeApplication.class, args);
	}

	/**
	 * @method run
	 * @description 애플리케이션 시작 시 실행되는 콜백 메서드입니다.
	 *              테스트 목적으로 기본 사용자 계정(user, admin)을 생성합니다.
	 * @param {String[]} args - 커맨드 라인 인자
	 * @throws {Exception} 실행 중 발생할 수 있는 예외
	 */
	@Override
	public void run(String... args) throws Exception {
		// 'user' 계정이 존재하지 않으면 생성합니다.
		if (userRepository.findByUsername("user").isEmpty()) {
			User user = User.builder()
					.username("user")
					.password(passwordEncoder.encode("password")) // 비밀번호를 BCrypt로 인코딩하여 저장
					.roles("ROLE_USER")
					.build();
			userRepository.save(user);
			System.out.println("Test user 'user' created with password 'password'");
		}
		// 'admin' 계정이 존재하지 않으면 생성합니다.
		if (userRepository.findByUsername("admin").isEmpty()) {
			User admin = User.builder()
					.username("admin")
					.password(passwordEncoder.encode("adminpass"))
					.roles("ROLE_ADMIN")
					.build();
			userRepository.save(admin);
			System.out.println("Test admin 'admin' created with password 'adminpass'");
		}
	}
}
