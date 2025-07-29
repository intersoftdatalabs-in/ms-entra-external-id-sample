package com.intsof.samples.security;

public class DatabaseSecurityProvider implements ISecurityProvider {

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        // Implement database authentication logic here
        return new AuthenticationResult(true, "userId123", "Authentication successful");
    }

    @Override
    public boolean supports(String type) {
        return "DB".equalsIgnoreCase(type);
    }

    @Override
    public void logout(String sessionId) {
        // Implement logout logic here
    }
}
