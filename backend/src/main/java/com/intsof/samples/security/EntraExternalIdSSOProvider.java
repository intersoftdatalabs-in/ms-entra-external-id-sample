package com.intsof.samples.security;

public class EntraExternalIdSSOProvider implements ISecurityProvider {

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        // Implement Entra External ID SSO authentication logic here
        return new AuthenticationResult(true, "entraUserId123", "Entra External ID SSO authentication successful");
    }

    @Override
    public boolean supports(String type) {
        return "EntraExternalIdSSO".equalsIgnoreCase(type);
    }

    @Override
    public void logout(String sessionId) {
        // Implement logout logic for Entra External ID SSO here
    }
    
}
