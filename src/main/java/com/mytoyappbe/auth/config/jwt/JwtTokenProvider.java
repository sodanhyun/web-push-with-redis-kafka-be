/**
 * @file JwtTokenProvider.java
 * @description JWT(JSON Web Token)의 생성, 검증, 정보 추출을 담당하는 유틸리티 클래스입니다.
 *              Access Token과 Refresh Token을 생성하고, 각 토큰의 유효성을 검증하며,
 *              토큰에서 사용자 인증 정보나 사용자 ID를 추출하는 기능을 제공합니다.
 */

package com.mytoyappbe.auth.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @class JwtTokenProvider
 * @description JWT(JSON Web Token)의 생성, 검증, 정보 추출을 담당하는 컴포넌트입니다.
 *              Spring Security와 연동하여 사용자 인증 및 권한 부여에 사용됩니다.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final Key key; // JWT 서명에 사용되는 비밀 키

    /**
     * @constructor JwtTokenProvider
     * @description JWT 비밀 키를 초기화하는 생성자입니다.
     *              `application.properties`에서 `jwt.secret` 값을 가져와 Base64 디코딩 후 HMAC SHA 키로 변환합니다.
     * @param secretKey - `application.properties`에 설정된 JWT 비밀 키
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * @method generateToken
     * @description 사용자 인증 정보(Authentication)를 기반으로 Access Token과 Refresh Token을 생성합니다.
     * @param {Authentication} authentication - Spring Security의 Authentication 객체 (인증된 사용자 정보 포함)
     * @returns {TokenInfo} 생성된 Access Token과 Refresh Token 정보를 담은 TokenInfo 객체
     */
    public TokenInfo generateToken(Authentication authentication) {
        // 사용자의 권한 정보를 쉼표로 구분된 문자열로 가져옵니다.
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime(); // 현재 시간 (밀리초)

        // Access Token 생성:
        // - Subject: 사용자 ID (authentication.getName())
        // - Claim ("auth"): 사용자의 권한 정보
        // - Expiration: 현재 시간으로부터 1일 후
        // - Signature: 설정된 비밀 키와 HS256 알고리즘으로 서명
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setExpiration(new Date(now + 86400000)) // Access Token 유효 기간: 1일
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성:
        // - Subject: 사용자 ID (authentication.getName()) - Refresh Token에도 사용자 식별 정보 포함
        // - Expiration: 현재 시간으로부터 7일 후
        // - Signature: 설정된 비밀 키와 HS256 알고리즘으로 서명
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(new Date(now + 86400000 * 7)) // Refresh Token 유효 기간: 7일
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * @method getAuthentication
     * @description Access Token을 복호화하여 토큰에 포함된 사용자 인증 정보(Authentication)를 추출합니다.
     * @param {String} accessToken - 복호화할 Access Token
     * @returns {Authentication} 토큰에서 추출된 사용자 인증 정보
     * @throws {RuntimeException} 권한 정보가 없는 토큰인 경우
     */
    public Authentication getAuthentication(String accessToken) {
        // Access Token에서 Claims(페이로드)를 파싱합니다.
        Claims claims = parseClaims(accessToken);

        // 토큰에 권한 정보("auth" 클레임)가 없으면 예외 발생
        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보를 가져와 GrantedAuthority 컬렉션으로 변환합니다.
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 생성하고 이를 기반으로 UsernamePasswordAuthenticationToken을 반환합니다.
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * @method validateToken
     * @description Access Token의 유효성을 검증합니다.
     *              토큰의 서명, 만료 여부 등을 확인합니다.
     * @param {String} token - 유효성을 검증할 Access Token
     * @returns {boolean} 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e); // 유효하지 않은 서명 또는 형식 오류
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e); // 만료된 토큰
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e); // 지원되지 않는 토큰 형식
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e); // 토큰 문자열이 비어있거나 null인 경우
        }
        return false;
    }

    /**
     * @method validateRefreshToken
     * @description Refresh Token의 유효성을 검증합니다.
     *              토큰의 서명, 만료 여부 등을 확인합니다.
     * @param {String} refreshToken - 유효성을 검증할 Refresh Token
     * @returns {boolean} Refresh Token이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid Refresh Token", e); // 유효하지 않은 서명 또는 형식 오류
        } catch (ExpiredJwtException e) {
            log.info("Expired Refresh Token", e); // 만료된 토큰
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported Refresh Token", e); // 지원되지 않는 토큰 형식
        } catch (IllegalArgumentException e) {
            log.info("Refresh Token claims string is empty.", e); // 토큰 문자열이 비어있거나 null인 경우
        }
        return false;
    }

    /**
     * @method getSubject
     * @description 주어진 토큰(Access Token 또는 Refresh Token)에서 사용자 ID(Subject)를 추출합니다.
     * @param {String} token - 사용자 ID를 추출할 토큰
     * @returns {String} 토큰에 포함된 사용자 ID
     */
    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * @method parseClaims
     * @description 주어진 Access Token에서 Claims(페이로드)를 파싱합니다.
     *              만료된 토큰의 경우에도 Claims를 반환하여 만료된 토큰의 정보를 활용할 수 있도록 합니다.
     * @param {String} accessToken - Claims를 파싱할 Access Token
     * @returns {Claims} 토큰의 페이로드(Claims) 객체
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰의 경우에도 Claims를 반환
        }
    }
}
