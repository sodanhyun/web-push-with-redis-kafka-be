/**
 * @file CustomUserDetailsService.java
 * @description Spring Security의 `UserDetailsService` 인터페이스를 구현하여
 *              사용자 이름(username)을 기반으로 사용자 상세 정보(UserDetails)를 로드하는 서비스입니다.
 *              주로 로그인 과정에서 사용자 인증을 위해 사용됩니다.
 */

package com.mytoyappbe.auth.config.security;

import com.mytoyappbe.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @class CustomUserDetailsService
 * @description Spring Security에서 사용자 인증을 위해 사용자 정보를 로드하는 서비스입니다.
 *              `UserDetailsService` 인터페이스를 구현하여 `loadUserByUsername` 메서드를 오버라이드합니다.
 */
@Service // Spring 서비스 컴포넌트임을 나타냅니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성합니다.
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // 사용자 데이터베이스 접근을 위한 리포지토리

    /**
     * @method loadUserByUsername
     * @description 주어진 사용자 이름(username)을 기반으로 사용자 상세 정보(UserDetails)를 로드합니다.
     *              사용자를 찾을 수 없는 경우 `UsernameNotFoundException`을 발생시킵니다.
     * @param {String} username - 조회할 사용자의 이름
     * @returns {UserDetails} 로드된 사용자 상세 정보
     * @throws {UsernameNotFoundException} 해당 사용자 이름으로 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // UserRepository를 통해 데이터베이스에서 사용자 정보를 조회합니다.
        // 사용자를 찾을 수 없는 경우 UsernameNotFoundException을 발생시킵니다.
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }
}