package com.example.ems.common.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoggingConfigurationTest {

    @Test
    @DisplayName("MaskingMessageConverter masks sensitive passwords, tokens, OTPs and secrets in JSON and text")
    void testSensitiveDataMasking() {
        // JSON format
        String jsonLog = "{\"email\":\"user@company.com\", \"password\":\"superSecretPassword123\", \"jwt\":\"eyJhbGciOi...\", \"otp\":\"123456\"}";
        String maskedJson = MaskingMessageConverter.mask(jsonLog);
        assertFalse(maskedJson.contains("superSecretPassword123"));
        assertFalse(maskedJson.contains("eyJhbGciOi..."));
        assertFalse(maskedJson.contains("123456"));
        assertTrue(maskedJson.contains("\"password\": \"******\""));
        assertTrue(maskedJson.contains("\"jwt\": \"******\""));
        assertTrue(maskedJson.contains("\"otp\": \"******\""));

        // Key-Value format
        String kvLog = "User login attempt with password=myPassword and otp: 987654 for user=alice";
        String maskedKv = MaskingMessageConverter.mask(kvLog);
        assertFalse(maskedKv.contains("myPassword"));
        assertFalse(maskedKv.contains("987654"));
        assertTrue(maskedKv.contains("password=******"));
        assertTrue(maskedKv.contains("otp: ******"));
        assertTrue(maskedKv.contains("user=alice"));

        // Bearer token
        String authHeaderLog = "Received request with Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xyz.abc";
        String maskedAuth = MaskingMessageConverter.mask(authHeaderLog);
        assertFalse(maskedAuth.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xyz.abc"));
        assertTrue(maskedAuth.contains("Bearer ******"));

        // Secrets
        String secretLog = "Configured Razorpay with keySecret: uq9yq68G6Dydj3FJXgobDe0 and webhookSecret: secret123";
        String maskedSecret = MaskingMessageConverter.mask(secretLog);
        assertFalse(maskedSecret.contains("uq9yq68G6Dydj3FJXgobDe0"));
        assertFalse(maskedSecret.contains("secret123"));
        assertTrue(maskedSecret.contains("keySecret: ******"));
        assertTrue(maskedSecret.contains("webhookSecret: ******"));
    }

    @Test
    @DisplayName("Verify Logback Spring context configured with required appenders, rolling policy and filters")
    void testLogbackAppenderConfiguration() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        Appender<?> appFileAppender = rootLogger.getAppender("APPLICATION_FILE");
        assertNotNull(appFileAppender, "APPLICATION_FILE appender should be registered on root logger");
        assertTrue(appFileAppender instanceof RollingFileAppender, "APPLICATION_FILE should be a RollingFileAppender");

        RollingFileAppender<?> rAppender = (RollingFileAppender<?>) appFileAppender;
        assertTrue(rAppender.getRollingPolicy() instanceof FixedWindowRollingPolicy, "Rolling policy should be FixedWindowRollingPolicy");
        FixedWindowRollingPolicy rollingPolicy = (FixedWindowRollingPolicy) rAppender.getRollingPolicy();
        assertEquals(1, rollingPolicy.getMinIndex());
        assertEquals(5, rollingPolicy.getMaxIndex(), "Maximum 5 archived files should be kept");
        assertTrue(rollingPolicy.getFileNamePattern().endsWith(".gz"), "Archived files must be compressed with .gz");

        assertTrue(rAppender.getTriggeringPolicy() instanceof SizeBasedTriggeringPolicy, "Triggering policy should be SizeBasedTriggeringPolicy");

        // Error Appender
        Appender<?> errFileAppender = rootLogger.getAppender("ERROR_FILE");
        assertNotNull(errFileAppender, "ERROR_FILE appender should be registered on root logger");
        assertTrue(errFileAppender instanceof RollingFileAppender, "ERROR_FILE should be a RollingFileAppender");

        RollingFileAppender<?> errRAppender = (RollingFileAppender<?>) errFileAppender;
        FixedWindowRollingPolicy errRollingPolicy = (FixedWindowRollingPolicy) errRAppender.getRollingPolicy();
        assertEquals(1, errRollingPolicy.getMinIndex());
        assertEquals(5, errRollingPolicy.getMaxIndex(), "Maximum 5 archived files should be kept for error logs");
        assertTrue(errRollingPolicy.getFileNamePattern().endsWith(".gz"), "Error archive files must be compressed with .gz");

        // Threshold filter on ERROR_FILE
        boolean hasErrorFilter = errRAppender.getCopyOfAttachedFiltersList().stream()
                .anyMatch(f -> f instanceof ThresholdFilter);
        assertTrue(hasErrorFilter, "ERROR_FILE appender must have a ThresholdFilter configured for ERROR level");
    }

    @Test
    @DisplayName("Verify log files are created and error logs are separated from general logs")
    void testLogFileCreationAndSeparation() throws Exception {
        org.slf4j.Logger logger = LoggerFactory.getLogger(LoggingConfigurationTest.class);

        String testInfoMsg = "TEST_INFO_LOG_" + System.currentTimeMillis();
        String testErrorMsg = "TEST_ERROR_LOG_" + System.currentTimeMillis();

        logger.info(testInfoMsg);
        logger.error(testErrorMsg);

        // Allow appender to flush
        Thread.sleep(200);

        Path appLogPath = Path.of("logs/ems-backend.log");
        Path errLogPath = Path.of("logs/ems-backend-error.log");

        assertTrue(Files.exists(appLogPath), "logs/ems-backend.log should exist");
        assertTrue(Files.exists(errLogPath), "logs/ems-backend-error.log should exist");

        String appLogContent = Files.readString(appLogPath);
        String errLogContent = Files.readString(errLogPath);

        assertTrue(appLogContent.contains(testInfoMsg), "Application log should contain INFO message");
        assertTrue(appLogContent.contains(testErrorMsg), "Application log should contain ERROR message");

        assertFalse(errLogContent.contains(testInfoMsg), "Error log must NOT contain INFO message");
        assertTrue(errLogContent.contains(testErrorMsg), "Error log MUST contain ERROR message");
    }

    @Test
    @DisplayName("Verify rolling policy retains maximum 5 archived files and compresses them with .gz")
    void testRollingPolicyExecutionAndRetention() throws Exception {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        RollingFileAppender<ch.qos.logback.classic.spi.ILoggingEvent> testAppender = new RollingFileAppender<>();
        testAppender.setContext(context);

        Path tempDir = Files.createTempDirectory("log_roll_test");
        Path activeLog = tempDir.resolve("test-app.log");
        testAppender.setFile(activeLog.toString());

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(testAppender);
        rollingPolicy.setFileNamePattern(tempDir.resolve("test-app.%i.log.gz").toString());
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(5);
        rollingPolicy.start();

        SizeBasedTriggeringPolicy<ch.qos.logback.classic.spi.ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(context);
        triggeringPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("1KB"));
        triggeringPolicy.start();

        ch.qos.logback.classic.encoder.PatternLayoutEncoder encoder = new ch.qos.logback.classic.encoder.PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg%n");
        encoder.start();

        testAppender.setRollingPolicy(rollingPolicy);
        testAppender.setTriggeringPolicy(triggeringPolicy);
        testAppender.setEncoder(encoder);
        testAppender.start();

        Logger testLogger = context.getLogger("RollTestLogger");
        testLogger.addAppender(testAppender);
        testLogger.setAdditive(false);

        // Perform 8 rollovers
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 30; j++) {
                testLogger.info("Log message payload line to fill buffer and trigger file rollover " + i + "-" + j);
            }
            testAppender.rollover();
        }

        testAppender.stop();

        long gzFilesCount;
        try (var stream = Files.list(tempDir)) {
            gzFilesCount = stream.filter(p -> p.toString().endsWith(".gz")).count();
        }

        assertEquals(5, gzFilesCount, "Exactly 5 archived .gz files should be retained after 8 rollovers");
        assertTrue(Files.exists(tempDir.resolve("test-app.1.log.gz")), "test-app.1.log.gz should exist");
        assertTrue(Files.exists(tempDir.resolve("test-app.5.log.gz")), "test-app.5.log.gz should exist");
        assertFalse(Files.exists(tempDir.resolve("test-app.6.log.gz")), "test-app.6.log.gz must NOT exist as oldest is deleted");
    }
}
