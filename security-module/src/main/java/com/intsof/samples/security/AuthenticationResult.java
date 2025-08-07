package com.intsof.samples.security;

import java.util.List;

/**
 * Represents the result of an authentication attempt.
 */
public class AuthenticationResult {
    private boolean success;
    private String userId;
    private String message;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private List<String> roles;

    public AuthenticationResult(boolean success, String userId, String message) {
        this(success, userId, message, null, null, 0, null);
    }

    public AuthenticationResult(boolean success, String userId, String message,
                                String accessToken, String refreshToken,
                                long expiresIn, List<String> roles) {
        this.success = success;
        this.userId = userId;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.roles = roles;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public List<String> getRoles() {
        return roles;
    }
}
