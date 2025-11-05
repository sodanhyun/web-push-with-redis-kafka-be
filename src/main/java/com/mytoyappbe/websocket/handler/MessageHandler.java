package com.mytoyappbe.websocket.handler;

public interface MessageHandler {

    boolean canHandle(String channel);

    void handle(String channel, String messageBody);
}
