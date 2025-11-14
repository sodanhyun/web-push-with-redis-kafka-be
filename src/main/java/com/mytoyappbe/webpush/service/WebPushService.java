package com.mytoyappbe.webpush.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.webpush.dto.PushSubscriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jose4j.lang.JoseException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

/**
 * 웹 푸시(Web Push) 구독 정보를 관리하고, 실제 푸시 알림을 전송하는 서비스 클래스입니다.
 * <p>
 * 클라이언트로부터 받은 푸시 구독 정보를 Redis에 저장하고, 필요할 때 Redis에서 구독 정보를 가져와
 * {@link PushService}를 사용하여 사용자에게 알림을 보냅니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    /**
     * Redis에 푸시 구독 정보를 저장할 때 사용할 해시(Hash) 키입니다.
     * 이 키 아래에 사용자 ID를 필드로 하여 각 사용자의 구독 정보(JSON 문자열)를 저장합니다.
     */
    private static final String REDIS_SUBSCRIPTION_HASH_KEY = "web-push-subscriptions-by-user";

    /**
     * Redis 데이터베이스와 상호작용하기 위한 {@link RedisTemplate}입니다.
     * 주로 {@code opsForHash()}를 사용하여 해시 데이터 구조에 구독 정보를 저장하고 조회합니다.
     */
    private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate<String, Object>로 변경

    /**
     * 웹 푸시 메시지를 외부 푸시 서비스(예: Google FCM)로 전송하는 핵심 서비스입니다.
     * VAPID 키를 사용하여 메시지를 서명하고 암호화합니다.
     */
    private final PushService pushService;

    /**
     * Java 객체를 JSON 문자열로 변환하거나 그 반대로 변환하기 위한 {@link ObjectMapper}입니다.
     * 푸시 구독 DTO를 Redis에 저장하기 위해 JSON으로 직렬화하고, Redis에서 가져온 JSON을 다시 DTO로 역직렬화하는 데 사용됩니다.
     */
    private final ObjectMapper objectMapper;

    /**
     * 클라이언트로부터 받은 푸시 구독 정보를 Redis에 저장합니다.
     * <p>
     * {@link PushSubscriptionDto} 객체를 JSON 문자열로 직렬화하여 Redis의 해시(Hash) 구조에 저장합니다.
     * 키는 {@code REDIS_SUBSCRIPTION_HASH_KEY}이고, 필드는 {@code subscriptionDto.getUserId()}입니다.
     *
     * @param subscriptionDto 저장할 푸시 구독 정보
     */
    public void saveSubscription(PushSubscriptionDto subscriptionDto, String userId) {
        try {
            String subscriptionJson = objectMapper.writeValueAsString(subscriptionDto);
            redisTemplate.opsForHash().put(REDIS_SUBSCRIPTION_HASH_KEY, userId, subscriptionJson);
            log.info("Subscription saved for user {}: {}", userId, subscriptionJson);
        } catch (JsonProcessingException e) {
            log.error("Error saving subscription for user {}", userId, e);
        }
    }

    /**
     * 특정 사용자에게 웹 푸시 알림을 전송합니다.
     * <p>
     * 1. Redis에서 해당 사용자의 푸시 구독 정보를 조회합니다.
     * 2. 조회된 JSON 문자열을 {@link PushSubscriptionDto} 객체로 역직렬화합니다.
     * 3. {@link PushService}를 사용하여 알림을 전송합니다.
     * 4. 알림 전송 결과(HTTP 상태 코드)에 따라 만료된 구독을 Redis에서 제거하는 등의 후처리를 수행합니다.
     *
     * @param userId 알림을 전송할 사용자의 ID
     * @param message 사용자에게 보낼 알림 메시지 내용
     */
    public void sendNotificationToUser(String userId, String message) {
        // Redis에서 해당 사용자의 구독 정보를 조회합니다.
        String subscriptionJson = (String) redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
        if (subscriptionJson == null) {
            log.info("No subscription found for user {}", userId);
            return;
        }

        try {
            // JSON 문자열을 PushSubscriptionDto 객체로 변환합니다.
            PushSubscriptionDto subDto = objectMapper.readValue(subscriptionJson, PushSubscriptionDto.class);
            // PushService 라이브러리에서 사용하는 Subscription 객체로 변환합니다.
            Subscription subscription = new Subscription(subDto.getEndpoint(), new Subscription.Keys(subDto.getKeys().getP256dh(), subDto.getKeys().getAuth()));

            // 1. 푸시 알림 페이로드(JSON)를 생성합니다.
            // 서비스 워커(sw.js)가 JSON 페이로드를 기대하므로, Map을 사용하여 구조를 만듭니다.
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("title", "새로운 알림");
            payload.put("body", message);
            payload.put("url", "/"); // 알림 클릭 시 이동할 URL

            // 2. Map을 JSON 문자열로 직렬화합니다.
            String payloadJson = objectMapper.writeValueAsString(payload);

            // 3. 직렬화된 JSON 문자열과 Urgency를 Notification 객체에 담아 전송합니다.
            // Urgency를 'high'로 설정하여 OS 수준의 절전 기능에 의한 지연을 최소화합니다.
            Notification notification = new Notification(subscription, payloadJson, nl.martijndwars.webpush.Urgency.HIGH);

            // PushService를 통해 알림을 전송하고 응답을 받습니다.
            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            // HTTP 상태 코드에 따른 후처리 로직
            if (statusCode == 410) { // GONE (410): 구독이 더 이상 유효하지 않음 (만료 또는 사용자 해지)
                log.info("Subscription for user {} expired or invalid. Removing.", userId);
                redisTemplate.opsForHash().delete(REDIS_SUBSCRIPTION_HASH_KEY, userId); // Redis에서 해당 구독 정보 삭제
            } else if (statusCode != 201) { // 201 CREATED: 성공적으로 알림이 전송되었음을 의미
                String responseBody = EntityUtils.toString(response.getEntity());
                log.warn("Failed to send push notification to user {}. Status: {}, Response: {}", userId, statusCode, responseBody);
            }

        } catch (JsonProcessingException e) {
            log.error("Error deserializing subscription for user {}", userId, e);
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
            log.error("Error sending push notification for user {}", userId, e);
        }
    }
}
