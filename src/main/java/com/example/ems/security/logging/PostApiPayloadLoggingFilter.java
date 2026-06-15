package com.example.ems.security.logging;

import com.example.ems.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Order(10) // Run after primary security filtering
public class PostApiPayloadLoggingFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PostApiPayloadLogRepository logRepository;

    private static final Object fileLock = new Object();
    private static final String LOG_FILE_PATH = "/home/subashini/Documents/ems-backend/post_api_payloads.log";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only log POST requests
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Exclude Swagger/documentation endpoints
        String uri = request.getRequestURI();
        if (uri.contains("/v3/api-docs") || uri.contains("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            try {
                processLog(wrappedRequest, wrappedResponse);
            } catch (Exception e) {
                // Ensure logging errors don't crash the actual request
                logger.error("Error occurred while logging POST API payload", e);
            }
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void processLog(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        LocalDateTime timestamp = LocalDateTime.now();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // Extract request payload
        String reqBody = getPayLoad(request.getContentAsByteArray(), request.getCharacterEncoding());
        if (reqBody.isBlank()) {
            reqBody = "[Empty]";
        }

        // Extract response payload
        String respBody = getPayLoad(response.getContentAsByteArray(), response.getCharacterEncoding());
        if (respBody.isBlank()) {
            respBody = "[Empty]";
        }

        // Extract authenticated user if available
        String userEmail = "Anonymous";
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                try {
                    userEmail = jwtService.getEmailFromToken(token);
                } catch (Exception e) {
                    // Ignore parsing exceptions
                }
            }
        }

        // 1. Save to Database
        try {
            PostApiPayloadLog logEntry = new PostApiPayloadLog(
                    timestamp,
                    uri,
                    reqBody,
                    respBody,
                    status,
                    userEmail
            );
            logRepository.save(logEntry);
        } catch (Exception e) {
            logger.error("Failed to save POST API payload log to database", e);
        }

        // 2. Save/Append to File
        writeToFile(timestamp, uri, userEmail, status, reqBody, respBody);
    }

    private String getPayLoad(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        try {
            return new String(content, encoding != null ? encoding : "UTF-8");
        } catch (Exception e) {
            return "[Error parsing content]";
        }
    }

    private void writeToFile(LocalDateTime timestamp, String uri, String userEmail, int status, String reqBody, String respBody) {
        synchronized (fileLock) {
            File logFile = new File(LOG_FILE_PATH);
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write("======================================================================\n");
                writer.write("Timestamp:        " + timestamp.toString() + "\n");
                writer.write("URI:              " + uri + "\n");
                writer.write("User:             " + userEmail + "\n");
                writer.write("Status:           " + status + "\n");
                writer.write("------------------------------- REQUEST ------------------------------\n");
                writer.write(reqBody + "\n");
                writer.write("------------------------------ RESPONSE ------------------------------\n");
                writer.write(respBody + "\n");
                writer.write("======================================================================\n\n");
            } catch (IOException e) {
                logger.error("Failed to write POST API payload log to file", e);
            }
        }
    }
}
