package com.mytoyappbe.manager;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void addSession(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public Optional<WebSocketSession> getSession(String userId) {
        return Optional.ofNullable(sessions.get(userId));
    }

    public void removeSession(String userId) {
        sessions.remove(userId);
    }
}
