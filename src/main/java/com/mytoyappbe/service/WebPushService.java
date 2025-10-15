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
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private static final String REDIS_SUBSCRIPTION_SET_KEY = "web-push-subscriptions";

    private final RedisTemplate<String, String> redisTemplate;
    private final PushService pushService;
    private final ObjectMapper objectMapper;

    public void saveSubscription(PushSubscriptionDto subscriptionDto) {
        try {
            String subscriptionJson = objectMapper.writeValueAsString(subscriptionDto);
            redisTemplate.opsForSet().add(REDIS_SUBSCRIPTION_SET_KEY, subscriptionJson);
            log.info("Subscription saved: {}", subscriptionJson);
        } catch (JsonProcessingException e) {
            log.error("Error saving subscription", e);
        }
    }

    public void sendNotificationToAll(String message) {
        Set<String> subscriptionJsons = redisTemplate.opsForSet().members(REDIS_SUBSCRIPTION_SET_KEY);
        if (subscriptionJsons == null || subscriptionJsons.isEmpty()) {
            log.info("No subscriptions to send notifications to.");
            return;
        }

        for (String subscriptionJson : subscriptionJsons) {
            try {
                PushSubscriptionDto subDto = objectMapper.readValue(subscriptionJson, PushSubscriptionDto.class);
                Subscription subscription = new Subscription(subDto.getEndpoint(), new Subscription.Keys(subDto.getKeys().getP256dh(), subDto.getKeys().getAuth()));

                Notification notification = new Notification(subscription, message);
                HttpResponse response = pushService.send(notification);
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 410) { // GONE: Subscription is no longer valid
                    log.info("Subscription expired or invalid. Removing: {}", subscriptionJson);
                    redisTemplate.opsForSet().remove(REDIS_SUBSCRIPTION_SET_KEY, subscriptionJson);
                } else if (statusCode != 201) { // 201 CREATED is success
                    String responseBody = EntityUtils.toString(response.getEntity());
                    log.warn("Failed to send push notification to {}. Status: {}, Response: {}", subDto.getEndpoint(), statusCode, responseBody);
                }

            } catch (JsonProcessingException e) {
                log.error("Error deserializing subscription: {}", subscriptionJson, e);
            } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
                log.error("Error sending push notification for subscription: {}", subscriptionJson, e);
            }
        }
    }
}
