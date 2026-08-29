package com.keshav.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.project-id:${FIREBASE_PROJECT_ID:}}")
    private String projectId;

    @Value("${firebase.credentials-json:${FIREBASE_CREDENTIALS_JSON:}}")
    private String credentialsJson;

    @Value("${firebase.config-path:${FIREBASE_CONFIG_PATH:}}")
    private String configPath;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp is already initialized.");
            return;
        }

        try {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();
            GoogleCredentials credentials = null;

            // 1. Try inline JSON credentials from ENV (ideal for Render, Docker, Heroku, etc.)
            if (credentialsJson != null && !credentialsJson.isBlank()) {
                try (InputStream is = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(is);
                    log.info("Firebase Admin initialized using FIREBASE_CREDENTIALS_JSON.");
                }
            }
            // 2. Try file path
            else if (configPath != null && !configPath.isBlank()) {
                try (InputStream is = new FileInputStream(configPath)) {
                    credentials = GoogleCredentials.fromStream(is);
                    log.info("Firebase Admin initialized using file path: {}", configPath);
                }
            }
            // 3. Try application default credentials if available
            else {
                try {
                    credentials = GoogleCredentials.getApplicationDefault();
                    log.info("Firebase Admin initialized using Application Default Credentials.");
                } catch (Exception e) {
                    log.warn("Application Default Credentials not found for Firebase: {}", e.getMessage());
                }
            }

            if (credentials != null) {
                optionsBuilder.setCredentials(credentials);
            }

            if (projectId != null && !projectId.isBlank()) {
                optionsBuilder.setProjectId(projectId);
            }

            FirebaseApp.initializeApp(optionsBuilder.build());
            log.info("Firebase Admin App initialized successfully.");

        } catch (Exception e) {
            log.warn("Firebase Admin SDK initialization skipped or failed: {}. (Firebase Auth token verification will attempt fallback if unconfigured).", e.getMessage());
        }
    }
}
