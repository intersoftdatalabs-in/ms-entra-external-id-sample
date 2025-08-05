package com.intsof.samples.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;

public class EntraExternalIdSSOProvider implements ISecurityProvider {

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        // Placeholder implementation – SSO flow not yet
        //Implement Microsoft Entra External ID login feature here
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
