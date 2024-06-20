package com.mipt.hsse.hssetechbackend.oauth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private static final String YANDEX_AUTHORIZATION_URI = "https://oauth.yandex.ru/authorize";
    private static final String YANDEX_TOKEN_URI = "https://oauth.yandex.ru/token";
    private static final String YANDEX_USER_INFO_URI = "https://login.yandex.ru/info";
    private static final String YANDEX_USERNAME_ATTR = "id";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Value("${OAUTH_SUCCESS_URL}") String successUrl,
                                                   @Value("${OAUTH_FAILURE_URL}") String failureUrl) throws Exception {
        return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl(successUrl, true)
                        .failureUrl(failureUrl))
                .build();
    }

    @Bean
    public ClientRegistrationRepository yandexOAuthClientRegistrationRepository(@Value("${OAUTH_YANDEX_CLIENT_ID}") String yandexClientId,
                                                                                @Value("${OAUTH_YANDEX_CLIENT_SECRET}") String yandexClientSecret,
                                                                                @Value("${OAUTH_YANDEX_REDIRECT_URI}") String yandexRedirectUri) {
        return new InMemoryClientRegistrationRepository(yandexOAuthClientRegistration(yandexClientId, yandexClientSecret, yandexRedirectUri));
    }

    private ClientRegistration yandexOAuthClientRegistration(String yandexClientId, String yandexClientSecret, String yandexRedirectUri) {
        return ClientRegistration.withRegistrationId("yandex")
                .clientId(yandexClientId)
                .clientSecret(yandexClientSecret)
                .redirectUri(yandexRedirectUri)
                .authorizationGrantType(AUTHORIZATION_CODE)
                .authorizationUri(YANDEX_AUTHORIZATION_URI)
                .tokenUri(YANDEX_TOKEN_URI)
                .userInfoUri(YANDEX_USER_INFO_URI)
                .userNameAttributeName(YANDEX_USERNAME_ATTR)
                .build();
    }
}
