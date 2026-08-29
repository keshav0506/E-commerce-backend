package com.keshav.controller;

import com.keshav.dto.*;
import com.keshav.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/firebase-sync")
    public ResponseEntity<LoginResponseDTO> firebaseSync(
            @RequestBody FirebaseSyncRequestDTO request) {

        return ResponseEntity.ok(
                authService.syncFirebaseUser(request)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponseDTO> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO(false, "Authentication required"));
        }

        return ResponseEntity.ok(
                authService.changePassword(authentication.getName(), request)
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDTO> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {

        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDTO> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {

        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }
}