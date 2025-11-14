/**
 * @file User.java
 * @description 사용자 정보를 나타내는 JPA 엔티티 클래스입니다.
 *              Spring Security의 `UserDetails` 인터페이스를 구현하여 인증 및 권한 부여에 사용됩니다.
 */

package com.mytoyappbe.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @class User
 * @description 사용자 정보를 데이터베이스에 매핑하는 JPA 엔티티이자,
 *              Spring Security에서 사용자 인증 및 권한 정보를 제공하는 `UserDetails` 구현체입니다.
 */
@Entity // 이 클래스가 JPA 엔티티임을 나타냅니다.
@Table(name = "users") // 데이터베이스 테이블 이름을 "users"로 지정합니다. ('user'는 SQL 예약어일 수 있습니다.)
@Data // Lombok 어노테이션: getter, setter, toString, equals, hashCode 메서드를 자동으로 생성합니다.
@Builder // Lombok 어노테이션: 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
@NoArgsConstructor // Lombok 어노테이션: 인자 없는 기본 생성자를 자동으로 생성합니다.
@AllArgsConstructor // Lombok 어노테이션: 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
public class User implements UserDetails {

    @Id // 기본 키(Primary Key)임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 데이터베이스에 위임합니다. (MySQL, H2 등)
    private Long id; // 사용자 고유 ID

    @Column(nullable = false, unique = true) // null을 허용하지 않으며, 유일한 값이어야 합니다.
    private String username; // 사용자 로그인 ID (고유해야 함)

    @Column(nullable = false) // null을 허용하지 않습니다.
    private String password; // 사용자 비밀번호 (암호화되어 저장됨)

    @Column(nullable = false) // null을 허용하지 않습니다.
    private String roles; // 사용자의 권한 (예: "ROLE_USER", "ROLE_ADMIN" 등 쉼표로 구분된 문자열)

    /**
     * @method getAuthorities
     * @description 사용자의 권한 목록을 반환합니다.
     *              `roles` 필드에 저장된 쉼표로 구분된 문자열을 `GrantedAuthority` 객체 컬렉션으로 변환합니다.
     * @returns {Collection<? extends GrantedAuthority>} 사용자의 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 단일 권한만 가정하고 처리합니다.
        // 여러 권한을 가질 경우:
        // return Arrays.stream(this.roles.split(","))
        //         .map(SimpleGrantedAuthority::new)
        //         .collect(Collectors.toList());
        return Collections.singletonList(new SimpleGrantedAuthority(this.roles));
    }

    /**
     * @method isAccountNonExpired
     * @description 계정의 만료 여부를 반환합니다. (true: 만료되지 않음)
     *              현재는 항상 만료되지 않은 것으로 설정되어 있습니다.
     * @returns {boolean} 계정 만료 여부
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @method isAccountNonLocked
     * @description 계정의 잠금 여부를 반환합니다. (true: 잠금되지 않음)
     *              현재는 항상 잠금되지 않은 것으로 설정되어 있습니다.
     * @returns {boolean} 계정 잠금 여부
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @method isCredentialsNonExpired
     * @description 자격 증명(비밀번호)의 만료 여부를 반환합니다. (true: 만료되지 않음)
     *              현재는 항상 만료되지 않은 것으로 설정되어 있습니다.
     * @returns {boolean} 자격 증명 만료 여부
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @method isEnabled
     * @description 계정의 활성화 여부를 반환합니다. (true: 활성화됨)
     *              현재는 항상 활성화된 것으로 설정되어 있습니다.
     * @returns {boolean} 계정 활성화 여부
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}