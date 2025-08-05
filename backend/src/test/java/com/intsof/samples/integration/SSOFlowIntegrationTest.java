package com.intsof.samples.integration;

import com.intsof.samples.security.SecurityManager;
import com.intsof.samples.security.DatabaseSecurityProvider;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import com.intsof.samples.security.AuthenticationResult;
import com.intsof.samples.entra.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for SSO Flow fix
 */
public class SSOFlowIntegrationTest {
    
    @Mock
    private UserService userService;
    
    private SecurityManager securityManager;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up security manager with both providers
        DatabaseSecurityProvider dbProvider = new DatabaseSecurityProvider(userService);
        EntraExternalIdSSOProvider ssoProvider = new EntraExternalIdSSOProvider();
        
        securityManager = new SecurityManager(dbProvider);
        securityManager.registerProvider("gmail.com", ssoProvider);
        securityManager.registerProvider("microsoft.com", ssoProvider);
    }
    
    @Test
    public void testSSOUserAuthenticationReturnsRedirectRequired() {
        // Test SSO user gets redirect response
        AuthenticationResult result = securityManager.authenticate("test@gmail.com", "password");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("SSO_REDIRECT_REQUIRED"));
    }
    
    @Test
    public void testRequiresSSO() {
        // Test domain checking
        assertTrue(securityManager.requiresSSO("test@gmail.com"));
        assertTrue(securityManager.requiresSSO("user@microsoft.com"));
        assertFalse(securityManager.requiresSSO("user@example.com"));
    }
    
    @Test
    public void testDatabaseUserAuthentication() {
        // Database users should still go through normal authentication
        AuthenticationResult result = securityManager.authenticate("test@example.com", "password");
        
        // Should not get SSO redirect (will fail authentication but that's expected without proper setup)
        assertFalse(result.getMessage().contains("SSO_REDIRECT_REQUIRED"));
    }
}
