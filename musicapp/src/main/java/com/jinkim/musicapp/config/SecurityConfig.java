package com.jinkim.musicapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig {

    // Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())             // Disable CSRF for development (enable in production)
            .cors(cors -> cors.configure(http))       // Enable CORS globally
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/api/playlists/**").permitAll()  // Allow unauthenticated access for auth and playlists
                .anyRequest().authenticated()         // Protect other endpoints
            )
            .formLogin(form -> form.disable())        // Disable default form login
            .httpBasic(basic -> basic.disable());     // Disable basic auth prompts

        return http.build();
    }

    // CORS Configuration
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")  // Allow requests from your frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")  // Expose token-related headers
                        .allowCredentials(true);          // Allow cookies and tokens
            }
        };
    }
}
