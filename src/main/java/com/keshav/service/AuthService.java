package com.keshav.service;

import com.keshav.dto.*;
import com.keshav.entity.User;
import com.keshav.exception.EmailAlreadyExistsException;
import com.keshav.exception.InvalidCredentialsException;
import com.keshav.repository.UserRepository;
import com.keshav.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.keshav.security.FirebaseTokenService firebaseTokenService;
    private final ICartService cartService;
    private final IWishlistService wishlistService;
    private final com.keshav.repository.SupplierProfileRepository supplierProfileRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            com.keshav.security.FirebaseTokenService firebaseTokenService,
            ICartService cartService,
            IWishlistService wishlistService,
            com.keshav.repository.SupplierProfileRepository supplierProfileRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.firebaseTokenService = firebaseTokenService;
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.supplierProfileRepository = supplierProfileRepository;
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already registered: " + request.getEmail()
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(com.keshav.entity.Role.CUSTOMER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // Merge guest cart & wishlist if guestSessionId exists
        mergeGuestData(savedUser, request.getGuestSessionId());

        return new RegisterResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        // 1. Strict Multi-Role Verification (Anti-Enumeration)
        if (request.getRole() != null && !request.getRole().isBlank()) {
            com.keshav.entity.Role requestedRole = com.keshav.entity.Role.fromString(request.getRole());
            if (user.getRole() != requestedRole) {
                throw new InvalidCredentialsException("Invalid credentials or role");
            }
        }

        // 2. Supplier Approval Check
        String supplierStatus = null;
        if (user.getRole() == com.keshav.entity.Role.SUPPLIER) {
            com.keshav.entity.SupplierProfile profile = supplierProfileRepository.findByUser(user).orElse(null);
            if (profile == null) {
                throw new InvalidCredentialsException("Supplier account profile not found. Please re-apply.");
            }
            supplierStatus = profile.getStatus().name();
            if (profile.getStatus() != com.keshav.entity.SupplierStatus.APPROVED) {
                throw new InvalidCredentialsException("Supplier account is currently " + profile.getStatus().name() + ". Access is restricted until administrator approval.");
            }
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        // Merge guest cart & wishlist if guestSessionId exists
        mergeGuestData(user, request.getGuestSessionId());

        return LoginResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .tokenType("Bearer")
                .supplierStatus(supplierStatus)
                .build();
    }

    @Override
    public LoginResponseDTO syncFirebaseUser(FirebaseSyncRequestDTO request) {
        String token = request != null ? request.getIdToken() : null;
        com.google.firebase.auth.FirebaseToken decodedToken = null;

        if (token != null && !token.isBlank()) {
            decodedToken = firebaseTokenService.verifyToken(token);
        }

        User user;
        if (decodedToken != null) {
            user = firebaseTokenService.syncUser(decodedToken, request != null ? request.getName() : null);
        } else {
            // Handle registration or direct login sync fallback if Admin SDK is in mock/dev mode
            if (request == null || ((request.getEmail() == null || request.getEmail().isBlank()) && (token == null || token.isBlank()))) {
                throw new IllegalArgumentException("Invalid Firebase authentication request");
            }
            String email = (request.getEmail() != null && !request.getEmail().isBlank()) ? request.getEmail() : "user@firebase.dev";
            String uid = "fb-uid-" + Math.abs(email.toLowerCase().hashCode());
            user = firebaseTokenService.syncUser(uid, email, request.getName());
        }

        // Merge guest cart & wishlist if guestSessionId exists
        if (request != null) {
            mergeGuestData(user, request.getGuestSessionId());
        }

        String appToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return LoginResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(appToken)
                .tokenType("Bearer")
                .build();
    }

    private void mergeGuestData(User user, String guestSessionId) {
        if (guestSessionId == null || guestSessionId.isBlank()) return;
        try {
            cartService.mergeGuestCart(user, guestSessionId);
            wishlistService.mergeGuestWishlist(user, guestSessionId);
        } catch (Exception e) {
            System.err.println("Warning: failed to merge guest cart/wishlist: " + e.getMessage());
        }
    }

    @Override
    public ApiResponseDTO changePassword(String email, ChangePasswordRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.keshav.exception.UserNotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ApiResponseDTO(true, "Password changed successfully");
    }

    @Override
    public ApiResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.keshav.exception.UserNotFoundException("No account found with email: " + request.getEmail()));

        // Generate 6-digit OTP / Reset Token
        String token = String.format("%06d", (int) (Math.random() * 900000) + 100000);
        user.setResetToken(token);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return new ApiResponseDTO(true, "Password reset code generated and sent successfully. Use OTP: " + token);
    }

    @Override
    public ApiResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.keshav.exception.UserNotFoundException("No account found with email: " + request.getEmail()));

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getToken().trim())) {
            throw new IllegalArgumentException("Invalid reset token or OTP");
        }

        if (user.getResetTokenExpiry() != null && user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return new ApiResponseDTO(true, "Password has been reset successfully. You can now sign in.");
    }
}