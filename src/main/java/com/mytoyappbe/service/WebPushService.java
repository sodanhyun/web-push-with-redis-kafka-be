package com.mytoyappbe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytoyappbe.dto.PushSubscriptionDto;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private static final String REDIS_SUBSCRIPTION_HASH_KEY = "web-push-subscriptions-by-user";

    private final RedisTemplate<String, String> redisTemplate;
    private final PushService pushService;
    private final ObjectMapper objectMapper;

    public void saveSubscription(PushSubscriptionDto subscriptionDto) {
        try {
            String subscriptionJson = objectMapper.writeValueAsString(subscriptionDto);
            redisTemplate.opsForHash().put(REDIS_SUBSCRIPTION_HASH_KEY, subscriptionDto.getUserId(), subscriptionJson);
            log.info("Subscription saved for user {}: {}", subscriptionDto.getUserId(), subscriptionJson);
        } catch (JsonProcessingException e) {
            log.error("Error saving subscription for user {}", subscriptionDto.getUserId(), e);
        }
    }

    public void sendNotificationToUser(String userId, String message) {
        String subscriptionJson = (String) redisTemplate.opsForHash().get(REDIS_SUBSCRIPTION_HASH_KEY, userId);
        if (subscriptionJson == null) {
            log.info("No subscription found for user {}", userId);
            return;
        }

        try {
            PushSubscriptionDto subDto = objectMapper.readValue(subscriptionJson, PushSubscriptionDto.class);
            Subscription subscription = new Subscription(subDto.getEndpoint(), new Subscription.Keys(subDto.getKeys().getP256dh(), subDto.getKeys().getAuth()));

            Notification notification = new Notification(subscription, message);
            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 410) { // GONE: Subscription is no longer valid
                log.info("Subscription for user {} expired or invalid. Removing.", userId);
                redisTemplate.opsForHash().delete(REDIS_SUBSCRIPTION_HASH_KEY, userId);
            } else if (statusCode != 201) { // 201 CREATED is success
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
