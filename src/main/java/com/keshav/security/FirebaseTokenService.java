package com.keshav.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.keshav.entity.User;
import com.keshav.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FirebaseTokenService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenService.class);

    private final UserRepository userRepository;

    public FirebaseTokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verify Firebase ID Token using Firebase Admin SDK.
     * Returns null if unverified or FirebaseApp not initialized.
     */
    public FirebaseToken verifyToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return null;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseApp is not initialized. Cannot verify token via Firebase Admin.");
            return null;
        }

        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            log.warn("Firebase ID token verification failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Idempotently find, create, or update user in MySQL based on verified Firebase credentials.
     * Role is never dictated by frontend; it defaults to CUSTOMER and preserves existing MySQL role (e.g. ADMIN).
     */
    @Transactional
    public User syncUser(String uid, String email, String name) {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("Firebase UID cannot be empty");
        }

        if (email == null || email.isBlank()) {
            email = uid + "@firebase.user";
        }

        // 1. Search by Firebase UID
        Optional<User> userByUidOpt = userRepository.findByFirebaseUid(uid);
        if (userByUidOpt.isPresent()) {
            User existing = userByUidOpt.get();
            boolean changed = false;
            if (name != null && !name.isBlank() && !name.equals(existing.getName())) {
                existing.setName(name);
                changed = true;
            }
            if (email != null && !email.isBlank() && !email.equalsIgnoreCase(existing.getEmail())) {
                existing.setEmail(email.toLowerCase());
                changed = true;
            }
            if (changed) {
                existing.setUpdatedAt(LocalDateTime.now());
                return userRepository.save(existing);
            }
            return existing;
        }

        // 2. Search by Email fallback (link existing account created previously)
        Optional<User> userByEmailOpt = userRepository.findByEmail(email.toLowerCase());
        if (userByEmailOpt.isPresent()) {
            User existing = userByEmailOpt.get();
            existing.setFirebaseUid(uid);
            if ((existing.getName() == null || existing.getName().isBlank()) && name != null && !name.isBlank()) {
                existing.setName(name);
            }
            existing.setUpdatedAt(LocalDateTime.now());
            log.info("Linked existing email account {} with Firebase UID {}", email, uid);
            return userRepository.save(existing);
        }

        // 3. Create brand new Customer user
        User newUser = new User();
        newUser.setFirebaseUid(uid);
        newUser.setEmail(email.toLowerCase());
        newUser.setName((name != null && !name.isBlank()) ? name : email.split("@")[0]);
        newUser.setRole(com.keshav.entity.Role.CUSTOMER);
        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(newUser);
        log.info("Created new MySQL customer user (id: {}, email: {}, firebaseUid: {})", saved.getId(), saved.getEmail(), saved.getFirebaseUid());
        return saved;
    }

    /**
     * Convenience method to sync user directly from a decoded FirebaseToken.
     */
    @Transactional
    public User syncUser(FirebaseToken token, String nameOverride) {
        String uid = token.getUid();
        String email = token.getEmail();
        String name = token.getName();
        if (name == null || name.isBlank()) {
            name = nameOverride;
        }
        return syncUser(uid, email, name);
    }
}
