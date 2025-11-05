package com.mytoyappbe.service.pubsub.handler;

/**
 * Redis Pub/Sub 메시지 처리를 위한 전략 인터페이스입니다.
 * 다양한 토픽 패턴에 따라 메시지를 처리하는 핸들러들이 이 인터페이스를 구현합니다.
 * {@link com.mytoyappbe.service.pubsub.RedisMessageSubscriber}는 이 인터페이스를 구현한 핸들러들을 찾아 메시지 처리를 위임합니다.
 */
public interface MessageHandler {

    /**
     * 주어진 채널(토픽)을 이 핸들러가 처리할 수 있는지 여부를 반환합니다.
     *
     * @param channel 메시지가 수신된 Redis 채널(토픽)
     * @return 이 핸들러가 채널을 처리할 수 있으면 true, 그렇지 않으면 false
     */
    boolean canHandle(String channel);

    /**
     * 수신된 메시지를 처리하는 메서드입니다.
     * 이 메서드는 {@link #canHandle(String)}이 true를 반환했을 때 호출됩니다.
     *
     * @param channel 메시지가 수신된 Redis 채널(토픽)
     * @param messageBody 수신된 메시지의 본문 (String 형태)
     */
    void handle(String channel, String messageBody);
}
