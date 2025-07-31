package com.intsof.samples.security;

public class EntraExternalIdSSOProvider implements ISecurityProvider {

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        // Placeholder implementation – SSO flow not yet implemented
        return new AuthenticationResult(false, null, "Entra External ID SSO authentication not implemented");
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
