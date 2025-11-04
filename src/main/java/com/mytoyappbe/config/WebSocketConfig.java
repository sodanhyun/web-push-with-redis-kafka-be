package com.mytoyappbe.config;

import com.mytoyappbe.handler.TestWebSocketHandler;
import com.mytoyappbe.manager.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(testWebSocketHandler(), "/ws/test/{userId}").setAllowedOrigins("*");
    }

    @Bean
    public TestWebSocketHandler testWebSocketHandler() {
        return new TestWebSocketHandler(webSocketSessionManager);
    }
}
