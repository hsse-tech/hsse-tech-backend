package com.mipt.hsse.hssetechbackend.oauth.services;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.UUID;

public abstract class OAuth2UserHelper {
    public static final String INNER_ID_ATTR = "inner_id";

    public static UUID getUserId(OAuth2User user) {
        return UUID.fromString(user.getAttributes().get(INNER_ID_ATTR).toString());
    }
}
