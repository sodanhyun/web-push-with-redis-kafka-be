package com.mytoyappbe.auth.controller;

import com.mytoyappbe.auth.config.jwt.TokenInfo;
import com.mytoyappbe.auth.dto.LoginRequestDto;
import com.mytoyappbe.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenInfo login(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("login request. username={}, password={}", loginRequestDto.getUsername(), loginRequestDto.getPassword());
        TokenInfo tokenInfo = authService.login(loginRequestDto);
        return tokenInfo;
    }
}
