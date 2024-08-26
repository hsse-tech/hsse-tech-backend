package com.mipt.hsse.hssetechbackend.oauth.config;

import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   MiptOAuth2UserService userService,
                                                   @Value("${cors.allowed-origins}") List<String> allowedOrigins) throws Exception {
        return http.authorizeHttpRequests(auth ->
                        auth
                            .requestMatchers("/api/locks/{id}/is-open",
                                            "/swagger-ui.html",
                                            "/actuator/health").permitAll()
                            .anyRequest().hasAuthority("ROLE_MIPT_USER"))
            .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/home", true)
                        .userInfoEndpoint(config -> config.userService(userService)))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(config -> config.configurationSource(corsConfiguration(allowedOrigins)))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfiguration(List<String> allowedOrigins) {
        var config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("*"));

        var urlSource = new UrlBasedCorsConfigurationSource();
        urlSource.registerCorsConfiguration("/**", config);

        return urlSource;
    }
}
