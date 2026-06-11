package com.example.ems.common.controller;

import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
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
public class MonitoringController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/health")
    public ResponseEntity<?> checkHealth() {
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

        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            String pingResult = connection.ping();
            isRedisUp = "PONG".equalsIgnoreCase(pingResult) || pingResult != null;
            healthInfo.put("redis", isRedisUp ? "UP" : "DOWN");
        } catch (Exception e) {
            healthInfo.put("redis", "DOWN (" + e.getMessage() + ")");
        }

        boolean systemUp = isDbUp && isRedisUp;
        healthInfo.put("status", systemUp ? "UP" : "DEGRADED");

        if (systemUp) {
            return ResponseEntity.ok(ApiResponse.success("System is healthy", healthInfo));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ErrorResponse.error("System is degraded", "SYS_503"));
        }
    }

    @GetMapping("/version")
    public ResponseEntity<?> getVersion() {
        Map<String, String> versionInfo = new LinkedHashMap<>();
        versionInfo.put("appName", "Employee Management System");
        versionInfo.put("version", "1.0.0");
        versionInfo.put("apiVersion", "v1");
        versionInfo.put("environment", "production");
        return ResponseEntity.ok(ApiResponse.success("App version details retrieved successfully", versionInfo));
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
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
