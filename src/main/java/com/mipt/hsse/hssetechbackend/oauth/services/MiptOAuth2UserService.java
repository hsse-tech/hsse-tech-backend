package com.mipt.hsse.hssetechbackend.oauth.services;

import com.mipt.hsse.hssetechbackend.oauth.PhystechDomainValidator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MiptOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private static final String LOGIN_ATTR = "login";
    private static final String NAME_ATTR = "id";

    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    private final UserPassportServiceBase passportService;

    public MiptOAuth2UserService(UserPassportServiceBase passportService) {
        this.passportService = passportService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        var loaded = defaultOAuth2UserService.loadUser(userRequest);
        var yandexIdObj = loaded.getName();

        try {
            var yandexId = Long.parseLong(yandexIdObj);
            var yandexLogin = Objects.requireNonNull(loaded.getAttribute(LOGIN_ATTR)).toString();

            if (!PhystechDomainValidator.isValid(yandexLogin)) {
                throw new OAuth2AuthenticationException("Invalid user profile (not \"@phystech.edu\" email suffix)");
            }

            var passport = passportService.findByYandexId(yandexId);
            var authorities = passport.getRoles()
                    .stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .toList();

            return new DefaultOAuth2User(authorities, loaded.getAttributes(), NAME_ATTR);
        } catch (NullPointerException | NumberFormatException e) {
            throw new OAuth2AuthenticationException("Invalid user profile");
        }
    }
}
