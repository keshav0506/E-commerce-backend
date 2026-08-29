package com.keshav.config;

import com.keshav.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required or token expired\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // AUTH
                        // =========================

                        .requestMatchers(
                                "/health",
                                "/api/health",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/firebase-sync",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/seed/**",
                                "/api/images/**"
                        ).permitAll()


                        // =========================
                        // REVIEWS (CUSTOMER & PUBLIC)
                        // =========================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products/*/reviews"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products/*/reviews"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/*/reviews"
                        ).authenticated()

                        .requestMatchers(
                                "/api/admin/reviews/**"
                        ).hasRole("ADMIN")


                        // =========================
                        // PRODUCTS
                        // =========================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/**"
                        ).hasRole("ADMIN")


                        // =========================
                        // CATEGORIES
                        // =========================

                        .requestMatchers(
                                "/api/categories/**",
                                "/api/contact",
                                "/uploads/**"
                        ).permitAll()


                        // =========================
                        // CART & WISHLIST
                        // =========================

                        .requestMatchers(
                                "/api/cart/**",
                                "/api/wishlist/**"
                        ).authenticated()


                        // =========================
                        // ORDERS
                        // =========================

                        .requestMatchers(
                                "/api/orders/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/payments/**")
                        .authenticated()

                        .requestMatchers(
                                "/api/addresses/**"
                        ).authenticated()


                        // =========================
                        // EVERYTHING ELSE
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/images/upload"
                        ).permitAll()

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}