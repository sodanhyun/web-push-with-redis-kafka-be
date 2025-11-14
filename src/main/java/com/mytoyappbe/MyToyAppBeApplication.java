package com.mytoyappbe;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mytoyappbe.auth.domain.User;
import com.mytoyappbe.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;

@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
public class MyToyAppBeApplication implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(MyToyAppBeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (userRepository.findByUsername("user").isEmpty()) {
			User user = User.builder()
					.username("user")
					.password(passwordEncoder.encode("password")) // 비밀번호 인코딩
					.roles("ROLE_USER")
					.build();
			userRepository.save(user);
			System.out.println("Test user 'user' created with password 'password'");
		}
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