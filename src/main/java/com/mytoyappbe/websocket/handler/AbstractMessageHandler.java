package com.mytoyappbe.websocket.handler;

public abstract class AbstractMessageHandler implements MessageHandler {
    private final String prefix;

    protected AbstractMessageHandler(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean canHandle(String channel) {
        return channel.startsWith(prefix);
    }

    protected String extractUserId(String channel) {
        return channel.substring(prefix.length());
    }
}
