package com.mipt.hsse.hssetechbackend.oauth.config;

import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, MiptOAuth2UserService userService) throws Exception {
    return http.authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority("ROLE_MIPT_USER"))
        .oauth2Login(
            oauth ->
                oauth
                    .defaultSuccessUrl("/home", true)
                    .userInfoEndpoint(config -> config.userService(userService)))
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }
}
