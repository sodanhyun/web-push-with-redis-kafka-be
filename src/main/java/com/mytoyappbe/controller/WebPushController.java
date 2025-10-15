package com.mytoyappbe.controller;

import com.mytoyappbe.dto.PushSubscriptionDto;
import com.mytoyappbe.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class WebPushController {

    private final WebPushService webPushService;

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@RequestBody PushSubscriptionDto subscription) {
        webPushService.saveSubscription(subscription);
    }
}
