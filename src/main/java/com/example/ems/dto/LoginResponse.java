package com.example.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private String timestamp;
    private LoginData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginData {
        private TokenData tokens;
        private UserData user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenData {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long accessTokenExpiresIn;
        private long refreshTokenExpiresIn;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private Long id;
        private String employeeId;
        private String name;
        private String email;
        private RoleData role;
        private List<String> permissions;
        private String status;
        private String lastLogin;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleData {
        private Long id;
        private String name;
    }
}
