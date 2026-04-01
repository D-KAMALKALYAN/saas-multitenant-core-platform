package com.saasplatform.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
