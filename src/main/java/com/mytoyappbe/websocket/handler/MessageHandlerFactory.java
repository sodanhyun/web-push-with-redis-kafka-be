package com.mytoyappbe.websocket.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MessageHandlerFactory {
    private final List<MessageHandler> handlers;

    @Autowired
    public MessageHandlerFactory(List<MessageHandler> handlers) {
        this.handlers = handlers;
    }

    public Optional<MessageHandler> getHandler(String channel) {
        return handlers.stream()
                       .filter(handler -> handler.canHandle(channel))
                       .findFirst();
    }
}
