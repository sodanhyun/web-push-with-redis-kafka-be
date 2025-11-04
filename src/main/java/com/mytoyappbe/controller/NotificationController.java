package com.mytoyappbe.controller;

import com.mytoyappbe.dto.KafkaNotificationMessageDto;
import com.mytoyappbe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public String sendNotification(@RequestBody KafkaNotificationMessageDto messageDto) {
        notificationService.sendNotification(messageDto);
        return "Notification sent successfully!";
    }
}
