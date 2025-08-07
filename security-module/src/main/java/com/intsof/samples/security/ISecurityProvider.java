package com.intsof.samples.security;

/**
 * Interface for authentication providers in a web application.
 * Supports multiple authentication mechanisms such as Database and SSO.
 */
public interface ISecurityProvider {

    /**
     * Authenticates a user using provided credentials.
     *
     * @param username the username or identifier
     * @param password the password or token (may be null for SSO)
     * @return an AuthenticationResult containing user details and status
     */
    AuthenticationResult authenticate(String username, String password);

    /**
     * Checks if the provider supports the given authentication type.
     *
     * @param type the authentication type (e.g., "DB", "SSO")
     * @return true if supported, false otherwise
     */
    boolean supports(String type);

    /**
     * Logs out the current user session.
     *
     * @param sessionId the session identifier
     */
    void logout(String sessionId);
}
