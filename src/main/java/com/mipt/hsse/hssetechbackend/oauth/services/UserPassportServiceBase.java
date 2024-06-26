package com.mipt.hsse.hssetechbackend.oauth.services;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface UserPassportServiceBase {
    HumanUserPassport findOrCreateByYandexId(OAuth2User user);
}
