package com.example.ems.storage.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.storage.bucket:}")
    private String storageBucket;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        String jsonKey = System.getenv("FIREBASE_KEY_JSON");
        if (jsonKey == null || jsonKey.trim().isEmpty()) {
            log.warn("FIREBASE_KEY_JSON environment variable is not set. Firebase Admin SDK will not be initialized.");
            return null;
        }

        try (InputStream serviceAccount = new ByteArrayInputStream(jsonKey.getBytes(StandardCharsets.UTF_8))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(storageBucket)
                    .build();

            log.info("Initializing Firebase Application with bucket: {}", storageBucket);
            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Failed to parse or initialize Firebase credentials: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Firebase credentials", e);
        }
    }
}
