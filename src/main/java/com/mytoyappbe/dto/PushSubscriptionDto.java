package com.mytoyappbe.dto;

import lombok.Getter;
import lombok.Setter;

public class PushSubscriptionDto {

    @Getter
    @Setter
    private String endpoint;

    @Getter
    @Setter
    private String userId;

    @Getter
    @Setter
    private Keys keys;

    @Getter
    @Setter
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
