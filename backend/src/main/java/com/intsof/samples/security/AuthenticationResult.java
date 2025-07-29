package com.intsof.samples.security;


/**
 * Represents the result of an authentication attempt.
 */
public class AuthenticationResult {
    private boolean success;
    private String userId;
    private String message;

    public AuthenticationResult(boolean success, String userId, String message) {
        this.success = success;
        this.userId = userId;
        this.message = message;
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
}
