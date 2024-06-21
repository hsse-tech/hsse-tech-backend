package com.mipt.hsse.hssetechbackend.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * Добавляет проверку на почту @phystech.edu
 */
@Component
public class MiptOAuth2UserService extends DefaultOAuth2UserService {
    private static final String LOGIN_ATTR = "login";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var principal = super.loadUser(userRequest);
        var loginAttr = principal.getAttribute(LOGIN_ATTR);

        if (loginAttr == null) {
            throw new OAuth2AuthenticationException("Missing required attribute: " + LOGIN_ATTR);
        }

        var login = loginAttr.toString();

        if (!PhystechDomainValidator.isValid(login)) {
            throw new OAuth2AuthenticationException("Invalid login email");
        }

        return principal;
    }
}
