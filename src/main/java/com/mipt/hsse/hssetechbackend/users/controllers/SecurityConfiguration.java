package com.mipt.hsse.hssetechbackend.users.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http//.csrf(AbstractHttpConfigurer::disable)
//                .csrf((csrf) -> csrf
//                        .ignoringRequestMatchers("/**"))
//                .headers(header -> {
//                    header.frameOptions(FrameOptionsConfig::disable);
//                })
                // Своего рода отключение CORS (разрешение запросов со всех доменов)
                .csrf().disable()
//                .cors(cors -> cors.configurationSource(request -> {
//                    var corsConfiguration = new CorsConfiguration();
//                    corsConfiguration.setAllowedOriginPatterns(List.of("*"));
//                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                    corsConfiguration.setAllowedHeaders(List.of("*"));
//                    corsConfiguration.setAllowCredentials(true);
//                    return corsConfiguration;
//                }))
                // Настройка доступа к конечным точкам
                .authorizeHttpRequests(request -> {
//                            request.anyRequest().permitAll();
                            // Можно указать конкретный путь, * - 1 уровень вложенности, ** - любое количество уровней вложенности
                            request.requestMatchers("/api/users/register",//todo manage
                                            // permissions
                                            "/api/users/auth", "/free",
                                            "/anyone","/yandex_oauth_callback","/yandex_oauth_callbackSecond").permitAll()
                                    .requestMatchers("/swagger-ui/**", "/swagger" +
                                                    "-resources/*", "/v3/api-docs/**",
                                            "/auth", "/register",
                                            "/useronly").hasRole(
                                            "USER")
                                    .requestMatchers("/endpoint", "/admin/**",
                                            "api/users/**", "/adminonly").hasRole(
                                            "ADMIN");
                        }
                )
//                .authorizeRequests()
//                .anyRequest().permitAll().and()
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    //@Bean
//public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http.addFilterBefore(jwtAuthenticationFilter,
//                    UsernamePasswordAuthenticationFilter.class)
//            .authorizeHttpRequests((request)->{
//                request.anyRequest().permitAll();
//            });
//    return http.build();
//}
//
//
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring()
//                .requestMatchers(new AntPathRequestMatcher("/**"));
//    }
}