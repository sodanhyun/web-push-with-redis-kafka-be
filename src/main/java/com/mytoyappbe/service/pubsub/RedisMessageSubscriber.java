package com.mytoyappbe.service.pubsub;

import com.mytoyappbe.service.pubsub.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis Pub/Sub 메시지를 구독하고, 수신된 메시지를 적절한 {@link MessageHandler} 구현체에 위임하는 서비스 클래스입니다.
 * 전략 패턴(Strategy Pattern)의 중재자(Dispatcher) 역할을 수행하여 메시지 처리 로직의 확장성을 높입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    /**
     * Spring에 의해 주입되는 모든 {@link MessageHandler} 구현체들의 리스트입니다.
     * 이 리스트를 순회하며 메시지를 처리할 수 있는 핸들러를 찾습니다.
     */
    private final List<MessageHandler> handlers;

    /**
     * Redis로부터 메시지를 수신했을 때 호출되는 콜백 메서드입니다.
     * 수신된 메시지의 채널을 기반으로 적절한 핸들러를 찾아 메시지 처리를 위임합니다.
     *
     * @param message 수신된 Redis 메시지 객체
     * @param pattern 메시지를 수신한 채널 패턴 (예: "ws:user:*")
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String messageBody = new String(message.getBody());

        for (MessageHandler handler : handlers) {
            if (handler.canHandle(channel)) {
                handler.handle(channel, messageBody);
                return;
            }
        }
        log.warn("No handler found for channel: {}", channel);
    }
}
