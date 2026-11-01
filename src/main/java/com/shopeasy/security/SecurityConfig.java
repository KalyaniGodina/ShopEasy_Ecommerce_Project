package com.shopeasy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Sign-in uses a SameSite=Lax session cookie, which blocks cross-site form posts.
            .csrf(AbstractHttpConfigurer::disable)
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))      // needed for the H2 console
            .authorizeHttpRequests(a -> a
                // admin only
                .requestMatchers(antMatcher("/h2-console/**")).hasRole("ADMIN")
                .requestMatchers(antMatcher(HttpMethod.POST, "/api/products/**")).hasRole("ADMIN")
                .requestMatchers(antMatcher(HttpMethod.PUT, "/api/products/**")).hasRole("ADMIN")
                .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/products/**")).hasRole("ADMIN")
                .requestMatchers(antMatcher(HttpMethod.GET, "/api/orders")).hasRole("ADMIN")
                // signed-in users
                .requestMatchers(antMatcher("/api/orders/**")).authenticated()
                // everything else: pages, images, product browsing, auth endpoints
                .anyRequest().permitAll())
            .exceptionHandling(e -> e.authenticationEntryPoint(
                    (req, res, ex) -> res.sendError(401, "Please sign in to continue")))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
