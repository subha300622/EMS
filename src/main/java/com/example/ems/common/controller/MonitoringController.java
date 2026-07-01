package com.example.ems.common.controller;

import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;

import com.example.ems.auth.service.SafeRedisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "System Administration")
public class MonitoringController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SafeRedisService safeRedisService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Object>> checkHealth() {
        Map<String, Object> healthInfo = new LinkedHashMap<>();
        boolean isDbUp = false;
        boolean isRedisUp = false;

        try {
            userRepository.count();
            isDbUp = true;
            healthInfo.put("database", "UP");
        } catch (Exception e) {
            healthInfo.put("database", "DOWN (" + e.getMessage() + ")");
        }

        isRedisUp = safeRedisService.isRedisAvailable();
        healthInfo.put("redis", isRedisUp ? "UP" : "DEGRADED");

        if (!isRedisUp) {
            healthInfo.put("impact", java.util.List.of(
                    "Caching is disabled",
                    "Session & OTP verification fallback to database",
                    "Rate limiting is running in soft/degraded mode"));
        }

        if (isDbUp) {
            String status = isRedisUp ? "UP" : "DEGRADED";
            healthInfo.put("status", status);
            return ResponseEntity
                    .ok(ApiResponse.success("System is " + (isRedisUp ? "healthy" : "degraded"), healthInfo));
        } else {
            healthInfo.put("status", "DOWN");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.success("System is down", healthInfo));
        }
    }

    @GetMapping("/version")
    public ResponseEntity<ApiResponse<Object>> getVersion() {
        Map<String, String> versionInfo = new LinkedHashMap<>();
        versionInfo.put("appName", "Employee Management System");
        versionInfo.put("version", "1.0.0");
        versionInfo.put("apiVersion", "v1");
        versionInfo.put("environment", "production");
        return ResponseEntity.ok(ApiResponse.success("App version details retrieved successfully", versionInfo));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Object>> getMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> memoryMetrics = new LinkedHashMap<>();
        memoryMetrics.put("totalMemoryBytes", totalMemory);
        memoryMetrics.put("freeMemoryBytes", freeMemory);
        memoryMetrics.put("maxMemoryBytes", maxMemory);
        memoryMetrics.put("usedMemoryBytes", usedMemory);
        memoryMetrics.put("usedMemoryPercentage", (double) usedMemory / totalMemory * 100);

        metrics.put("memory", memoryMetrics);

        metrics.put("activeThreads", Thread.activeCount());
        metrics.put("availableProcessors", runtime.availableProcessors());
        metrics.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(ApiResponse.success("System metrics retrieved successfully", metrics));
    }
}
