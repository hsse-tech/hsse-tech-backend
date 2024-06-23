package com.mipt.hsse.hssetechbackend.oauth.config;

import com.mipt.hsse.hssetechbackend.oauth.MiptOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   MiptOAuth2UserService miptUserService) throws Exception {
        return http.authorizeHttpRequests(auth ->
                        auth.requestMatchers("**").hasAuthority("MIPT_USER"))
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(endpoint -> endpoint.userService(miptUserService))
                        .defaultSuccessUrl("/home", true))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
