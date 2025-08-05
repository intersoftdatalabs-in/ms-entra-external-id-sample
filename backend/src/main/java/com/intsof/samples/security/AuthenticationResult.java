package com.intsof.samples.security;


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
    private java.util.List<String> roles;

    public AuthenticationResult(boolean success, String userId, String message) {
        this.success = success;
        this.userId = userId;
        this.message = message;
    }

    public AuthenticationResult(boolean success, String userId, String message, String accessToken, String refreshToken, long expiresIn, java.util.List<String> roles) {
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

    public java.util.List<String> getRoles() {
        return roles;
    }
}
