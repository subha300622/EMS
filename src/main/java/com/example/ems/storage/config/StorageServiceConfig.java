package com.example.ems.storage.config;

import com.example.ems.storage.service.FirebaseStorageService;
import com.example.ems.storage.service.FirebaseStorageServiceImpl;
import com.example.ems.storage.service.MockStorageServiceImpl;
import com.google.firebase.FirebaseApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class StorageServiceConfig {

    @Autowired(required = false)
    private FirebaseApp firebaseApp;

    @Bean
    @Primary
    public FirebaseStorageService firebaseStorageService(
            FirebaseStorageServiceImpl prodService,
            MockStorageServiceImpl mockService) {
        String jsonKey = System.getenv("FIREBASE_KEY_JSON");
        if (jsonKey != null && !jsonKey.trim().isEmpty() && firebaseApp != null) {
            return prodService;
        } else {
            return mockService;
        }
    }
}
