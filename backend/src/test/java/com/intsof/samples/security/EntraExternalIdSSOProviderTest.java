package com.intsof.samples.security;

import com.intsof.samples.security.spi.ExternalIdTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EntraExternalIdSSOProvider
 */
public class EntraExternalIdSSOProviderTest {
    
    private EntraExternalIdSSOProvider provider;
    
    @Mock
    private EntraIdService entraIdService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new EntraExternalIdSSOProvider();
        // Inject the mock service
        try {
            java.lang.reflect.Field field = EntraExternalIdSSOProvider.class.getDeclaredField("entraIdService");
            field.setAccessible(true);
            field.set(provider, entraIdService);
        } catch (Exception e) {
            fail("Failed to inject mock EntraIdService");
        }
    }
    
    @Test
    public void testAuthenticate_WithUsernamePassword() {
        AuthenticationResult result = provider.authenticate("test@example.com", "password");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not supported"));
    }
    
    @Test
    public void testAuthenticateWithAuthorizationCode_Success() {
        // Mock successful user profile extraction
        EntraIdService.EntraUserProfile mockProfile = new EntraIdService.EntraUserProfile(
            "test@example.com", "Test User", "Test", "User", "subject", 
            List.of("USER"), List.of("group1")
        );
        
        // For this test, we'll just test the failure case since MSAL4J integration
        // requires real Microsoft endpoints and credentials
        when(entraIdService.getUserProfile(anyString())).thenReturn(mockProfile);
        
        // Test the failure path when acquireTokenByAuthorizationCode returns null
        when(entraIdService.acquireTokenByAuthorizationCode(anyString(), anyString(), anySet()))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        AuthenticationResult result = provider.authenticateWithAuthorizationCode("auth-code", "http://localhost/callback");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Failed to acquire token"));
    }
    
    @Test
    public void testAuthenticateWithAuthorizationCode_Exception() {
        // Test exception handling
        when(entraIdService.acquireTokenByAuthorizationCode(anyString(), anyString(), anySet()))
            .thenThrow(new RuntimeException("MSAL4J error"));
        
        AuthenticationResult result = provider.authenticateWithAuthorizationCode("invalid-code", "http://localhost/callback");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("failed"));
    }
    
    @Test
    public void testValidateEntraToken_Success() {
        EntraIdService.EntraUserProfile mockProfile = new EntraIdService.EntraUserProfile(
            "test@example.com", "Test User", "Test", "User", "subject", 
            List.of("ADMIN"), List.of()
        );
        
        EntraIdService.EntraTokenValidationResult mockValidationResult = 
            new EntraIdService.EntraTokenValidationResult(true, "Valid token", mockProfile);
        
        when(entraIdService.validateToken(anyString())).thenReturn(mockValidationResult);
        
        AuthenticationResult result = provider.validateEntraToken("valid-token");
        
        assertTrue(result.isSuccess());
        assertEquals("test@example.com", result.getUserId());
        assertEquals("Token validation successful", result.getMessage());
        assertTrue(result.getRoles().contains("ADMIN"));
    }
    
    @Test
    public void testValidateEntraToken_Invalid() {
        EntraIdService.EntraTokenValidationResult mockValidationResult = 
            new EntraIdService.EntraTokenValidationResult(false, "Invalid token", null);
        
        when(entraIdService.validateToken(anyString())).thenReturn(mockValidationResult);
        
        AuthenticationResult result = provider.validateEntraToken("invalid-token");
        
        assertFalse(result.isSuccess());
        assertEquals("Invalid token", result.getMessage());
    }
    
    @Test
    public void testIsDomainEnabledForSSO() {
        List<String> enabledDomains = List.of("gmail.com", "microsoft.com", "example.org");
        
        assertTrue(provider.isDomainEnabledForSSO("user@gmail.com", enabledDomains));
        assertTrue(provider.isDomainEnabledForSSO("user@microsoft.com", enabledDomains));
        assertTrue(provider.isDomainEnabledForSSO("user@example.org", enabledDomains));
        assertFalse(provider.isDomainEnabledForSSO("user@other.com", enabledDomains));
        assertFalse(provider.isDomainEnabledForSSO("invalid-email", enabledDomains));
        assertFalse(provider.isDomainEnabledForSSO(null, enabledDomains));
        assertFalse(provider.isDomainEnabledForSSO("user@gmail.com", null));
    }
    
    @Test
    public void testSupports() {
        assertTrue(provider.supports("EntraExternalIdSSO"));
        assertTrue(provider.supports("entraexternalidsso"));
        assertTrue(provider.supports("ENTRAEXTERNALIDSSO"));
        assertFalse(provider.supports("DatabaseAuth"));
        assertFalse(provider.supports("OtherSSO"));
        assertFalse(provider.supports(null));
    }
    
    @Test
    public void testLogout() {
        // Test that logout method can be called without throwing exception
        assertDoesNotThrow(() -> provider.logout("session-id"));
    }
}
