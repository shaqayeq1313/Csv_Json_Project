package com.example.csv_json_project.springSecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF protection globally
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/csv/upload").permitAll()  // Allow access to /api/csv/upload without authentication
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
                
            		  )
            .sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers -> headers
                .frameOptions(FrameOptionsConfig::disable) // Disable frame options for H2 console
            );
        return http.build();
    }
}