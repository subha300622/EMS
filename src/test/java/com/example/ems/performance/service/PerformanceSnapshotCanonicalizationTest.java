package com.example.ems.performance.service;

import com.example.ems.performance.dto.PerformanceSnapshotDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceSnapshotCanonicalizationTest {

    private PerformanceSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        snapshotService = new PerformanceSnapshotService();
    }

    @Test
    @DisplayName("Numeric canonicalization: 96.92 and 96.920 produce identical canonical JSON and SHA-256 hash")
    void testNumericCanonicalization_ScaleInvariance() {
        Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put("score", new BigDecimal("96.92"));

        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("score", new BigDecimal("96.920"));

        String json1 = snapshotService.toCanonicalJson(map1);
        String json2 = snapshotService.toCanonicalJson(map2);

        assertEquals(json1, json2);
        assertEquals(snapshotService.calculateSha256(json1), snapshotService.calculateSha256(json2));
    }

    @Test
    @DisplayName("Property order canonicalization: different key ordering produces identical canonical JSON and hash")
    void testPropertyOrderCanonicalization() {
        Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put("zeta", "last");
        map1.put("alpha", "first");
        map1.put("beta", 42);

        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("alpha", "first");
        map2.put("beta", 42);
        map2.put("zeta", "last");

        String json1 = snapshotService.toCanonicalJson(map1);
        String json2 = snapshotService.toCanonicalJson(map2);

        assertEquals(json1, json2);
        assertEquals(snapshotService.calculateSha256(json1), snapshotService.calculateSha256(json2));
    }

    @Test
    @DisplayName("Snapshot tampering: modified value changes SHA-256 hash and fails verifyIntegrity")
    void testSnapshotIntegrity_TamperingDetected() {
        PerformanceSnapshotDto dto = new PerformanceSnapshotDto();
        dto.getEmployee().setEmployeeId(101L);
        dto.getEmployee().setName("Alice Smith");
        dto.getCycle().setCycleId(1L);
        dto.getCycle().setCode("CYC-2026-Q1");
        dto.getAttendance().setAttendanceScore(new BigDecimal("95.00"));

        String canonicalJson = snapshotService.serializeToJson(dto);
        String validHash = snapshotService.computeCanonicalHash(dto);

        assertTrue(snapshotService.verifyIntegrity(canonicalJson, validHash));

        // Tamper with the attendance score
        String tamperedJson = canonicalJson.replace("95", "99");
        assertFalse(snapshotService.verifyIntegrity(tamperedJson, validHash));
    }
}
