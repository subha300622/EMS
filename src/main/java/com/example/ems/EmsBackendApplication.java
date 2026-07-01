package com.example.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class EmsBackendApplication {

    public static void main(String[] args) {
        String rawUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("DATABASE_URL");
        }
        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                String cleanUrl = rawUrl.substring(rawUrl.indexOf("://") + 3);
                String[] authAndHost = cleanUrl.split("@");
                if (authAndHost.length == 2) {
                    String[] credentials = authAndHost[0].split(":");
                    String username = credentials[0];
                    String password = credentials.length > 1 ? credentials[1] : "";
                    
                    String hostAndDb = authAndHost[1];
                    String jdbcUrl = "jdbc:postgresql://" + hostAndDb;
                    
                    System.setProperty("spring.datasource.url", jdbcUrl);
                    System.setProperty("spring.datasource.username", username);
                    System.setProperty("spring.datasource.password", password);
                    System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
                    System.out.println("Dynamically configured PostgreSQL DataSource from environment variable.");
                }
            } catch (Exception e) {
                System.err.println("Failed to parse database URL: " + e.getMessage());
            }
        }
        SpringApplication.run(EmsBackendApplication.class, args);
    }
}
