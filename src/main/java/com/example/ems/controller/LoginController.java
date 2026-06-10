package com.example.ems.controller;

import com.example.ems.dto.LoginRequest;
import com.example.ems.dto.LoginResponse;
import com.example.ems.dto.RegisterRequest;
import com.example.ems.dto.ApiResponse;
import com.example.ems.dto.ErrorResponse;
import com.example.ems.entity.User;
import com.example.ems.service.JwtService;
import com.example.ems.service.SessionService;
import com.example.ems.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionService sessionService;

    /** POST /api/login */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        User user = userService.login(request.getEmail(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Invalid credentials", "AUTH_001"));
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : user.getRequestedRole();
        
        // Generate Tokens
        String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getWorkEmail(), roleName);
        
        // Create session in Redis
        SessionService.SessionMetadata session = sessionService.createSession(
                user.getUserId(),
                user.getWorkEmail(),
                httpRequest.getHeader("User-Agent"),
                getClientIp(httpRequest)
        );

        LoginResponse.TokenData tokenData = new LoginResponse.TokenData(
                accessToken,
                session.getRefreshToken(),
                "Bearer",
                900,
                604800
        );

        LoginResponse.RoleData roleData = null;
        List<String> permissions = new java.util.ArrayList<>();
        if (user.getRole() != null) {
            roleData = new LoginResponse.RoleData(user.getRole().getId(), user.getRole().getName());
            permissions = user.getRole().getPermissions().stream()
                    .map(com.example.ems.entity.Permission::getName)
                    .collect(Collectors.toList());
        }

        LoginResponse.UserData userData = new LoginResponse.UserData(
                user.getId(),
                user.getUserId(),
                user.getFullName(),
                user.getWorkEmail(),
                roleData,
                permissions,
                "ACTIVE",
                Instant.now().toString()
        );

        LoginResponse.LoginData loginData = new LoginResponse.LoginData(tokenData, userData);
        LoginResponse responseBody = new LoginResponse(true, "Login successful", Instant.now().toString(), loginData);

        return ResponseEntity.ok(responseBody);
    }

    /** POST /api/register */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        String result = userService.register(request);
        if (result.startsWith("Registration Successful!")) {
            return ResponseEntity.ok(ApiResponse.success(result));
        } else {
            return ResponseEntity.badRequest().body(ErrorResponse.error(result, "AUTH_016"));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}