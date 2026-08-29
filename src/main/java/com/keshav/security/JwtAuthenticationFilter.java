package com.keshav.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final FirebaseTokenService firebaseTokenService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            FirebaseTokenService firebaseTokenService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.firebaseTokenService = firebaseTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || "null".equalsIgnoreCase(token) || "undefined".equalsIgnoreCase(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Check if token is a valid Firebase ID Token
        try {
            com.google.firebase.auth.FirebaseToken decodedToken = firebaseTokenService.verifyToken(token);
            if (decodedToken != null) {
                com.keshav.entity.User user = firebaseTokenService.syncUser(decodedToken, null);
                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception ignored) {
            // Token is not a Firebase token or Firebase verification threw, fallback to JWT check
        }

        // 2. Check if token is a valid Spring Boot custom JWT token
        try {
            if (jwtService.isTokenValid(token)) {
                String email = jwtService.extractEmail(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ignored) {
            // Invalid JWT token
        }

        filterChain.doFilter(request, response);
    }
}