package com.example.ems.auth.dto;

import java.util.List;

public class LoginResponse {
    private boolean success;
    private String message;
    private String timestamp;
    private LoginData data;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String timestamp, LoginData data) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }

    public static class LoginData {
        private TokenData tokens;
        private UserData user;

        public LoginData() {}

        public LoginData(TokenData tokens, UserData user) {
            this.tokens = tokens;
            this.user = user;
        }

        public TokenData getTokens() {
            return tokens;
        }

        public void setTokens(TokenData tokens) {
            this.tokens = tokens;
        }

        public UserData getUser() {
            return user;
        }

        public void setUser(UserData user) {
            this.user = user;
        }
    }

    public static class TokenData {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long accessTokenExpiresIn;
        private long refreshTokenExpiresIn;

        public TokenData() {}

        public TokenData(String accessToken, String refreshToken, String tokenType, long accessTokenExpiresIn, long refreshTokenExpiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
            this.accessTokenExpiresIn = accessTokenExpiresIn;
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public long getAccessTokenExpiresIn() {
            return accessTokenExpiresIn;
        }

        public void setAccessTokenExpiresIn(long accessTokenExpiresIn) {
            this.accessTokenExpiresIn = accessTokenExpiresIn;
        }

        public long getRefreshTokenExpiresIn() {
            return refreshTokenExpiresIn;
        }

        public void setRefreshTokenExpiresIn(long refreshTokenExpiresIn) {
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        }
    }

    public static class UserData {
        private Long id;
        private String employeeId;
        private String name;
        private String email;
        private String role;
        private List<String> permissions;
        private String status;
        private String lastLogin;

        public UserData() {}

        public UserData(Long id, String employeeId, String name, String email, String role, List<String> permissions, String status, String lastLogin) {
            this.id = id;
            this.employeeId = employeeId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.permissions = permissions;
            this.status = status;
            this.lastLogin = lastLogin;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(String lastLogin) {
            this.lastLogin = lastLogin;
        }
    }
}
