package com.example.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class EmsBackendApplication {

    public static void main(String[] args) {
        String rawUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("DATABASE_URL");
        }
        if (rawUrl != null && !rawUrl.isBlank()) {
            try {
                if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                    String cleanUrl = rawUrl.substring(rawUrl.indexOf("://") + 3);
                    String[] authAndHost = cleanUrl.split("@");
                    if (authAndHost.length == 2) {
                        String[] credentials = authAndHost[0].split(":");
                        String username = credentials[0];
                        String password = credentials.length > 1 ? credentials[1] : "";
                        
                        String hostAndDb = authAndHost[1];
                        String resolvedHostAndDb = resolveHostAndDb(hostAndDb);
                        String jdbcUrl = "jdbc:postgresql://" + resolvedHostAndDb;
                        
                        System.setProperty("spring.datasource.url", jdbcUrl);
                        System.setProperty("spring.datasource.username", username);
                        System.setProperty("spring.datasource.password", password);
                        System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
                        System.out.println("Dynamically configured PostgreSQL DataSource from postgres:// env variable.");
                    }
                } else if (rawUrl.startsWith("jdbc:postgresql://")) {
                    String hostAndDb = rawUrl.substring("jdbc:postgresql://".length());
                    String resolvedHostAndDb = resolveHostAndDb(hostAndDb);
                    String jdbcUrl = "jdbc:postgresql://" + resolvedHostAndDb;
                    
                    System.setProperty("spring.datasource.url", jdbcUrl);
                    System.out.println("Dynamically configured PostgreSQL DataSource from jdbc:// env variable.");
                }
            } catch (Exception e) {
                System.err.println("Failed to parse database URL: " + e.getMessage());
            }
        }
        SpringApplication.run(EmsBackendApplication.class, args);
    }

    private static String resolveHostAndDb(String hostAndDb) {
        String hostAndPort;
        String database = "";
        int slashIdx = hostAndDb.indexOf('/');
        if (slashIdx != -1) {
            hostAndPort = hostAndDb.substring(0, slashIdx);
            database = hostAndDb.substring(slashIdx);
        } else {
            hostAndPort = hostAndDb;
        }
        
        String host;
        String port = "";
        int colonIdx = hostAndPort.indexOf(':');
        if (colonIdx != -1) {
            host = hostAndPort.substring(0, colonIdx);
            port = hostAndPort.substring(colonIdx);
        } else {
            host = hostAndPort;
        }
        
        String resolvedHost = resolveHostIfNeeded(host);
        return resolvedHost + port + database;
    }

    private static String resolveHostIfNeeded(String host) {
        if (host == null || host.contains(".")) {
            return host;
        }
        
        try {
            java.net.InetAddress.getByName(host);
            return host;
        } catch (java.net.UnknownHostException e) {
            String[] suffixes = {
                ".oregon-postgres.render.com",
                ".singapore-postgres.render.com",
                ".frankfurt-postgres.render.com",
                ".ohio-postgres.render.com"
            };
            for (String suffix : suffixes) {
                String candidate = host + suffix;
                try {
                    java.net.InetAddress.getByName(candidate);
                    System.out.println("Auto-detected Render PostgreSQL external host: " + candidate);
                    return candidate;
                } catch (java.net.UnknownHostException ex) {
                    // Ignore and try next
                }
            }
        }
        return host;
    }
}
