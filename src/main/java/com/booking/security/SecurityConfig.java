package com.booking.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))
            
            .exceptionHandling(exceptions ->
            exceptions
                    .authenticationEntryPoint(
                            jwtAuthenticationEntryPoint
                    )
                    .accessDeniedHandler(
                            customAccessDeniedHandler
                    )
    )

            .authorizeHttpRequests(auth -> auth

                // Authentication
                .requestMatchers("/auth/**").permitAll()

                // Swagger / OpenAPI
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()

                // =========================
                // RESOURCE ENDPOINTS
                // =========================

                // USER + ADMIN can read resources
                .requestMatchers(
                        HttpMethod.GET,
                        "/resources/**"
                ).authenticated()

                // ADMIN only
                .requestMatchers(
                        HttpMethod.POST,
                        "/resources/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/resources/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/resources/**"
                ).hasRole("ADMIN")

                // =========================
                // RESERVATION ENDPOINTS
                // =========================

                // USER creates reservation
                .requestMatchers(
                        HttpMethod.POST,
                        "/reservations"
                ).hasRole("USER")

                // USER's own reservations
                .requestMatchers(
                        HttpMethod.GET,
                        "/reservations/my"
                ).hasRole("USER")

                // ADMIN gets all reservations
                .requestMatchers(
                        HttpMethod.GET,
                        "/reservations"
                ).hasRole("ADMIN")

                // Individual reservation:
                // ownership will be checked in service layer
                .requestMatchers(
                        HttpMethod.GET,
                        "/reservations/*"
                ).authenticated()

                // USER can cancel own reservation / ADMIN can cancel any reservation
                .requestMatchers(
                        HttpMethod.PATCH,
                        "/reservations/*/cancel"
                ).authenticated()

                // ADMIN changes reservation status
                .requestMatchers(
                        HttpMethod.PATCH,
                        "/reservations/*/status"
                ).hasRole("ADMIN")

                // ADMIN deletes reservation
                .requestMatchers(
                        HttpMethod.DELETE,
                        "/reservations/**"
                ).hasRole("ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}