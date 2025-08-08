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
 * Unit tests for {@link EntraExternalIdSSOProvider} that rely solely on the
 * contract defined by {@link ExternalIdTokenService}. This keeps the security
 * module completely decoupled from any concrete implementation that might live
 * in the micro-service layer (e.g. the spring-boot `EntraIdService`).
 */
public class EntraExternalIdSSOProviderTest {

    private EntraExternalIdSSOProvider provider;

    @Mock
    private ExternalIdTokenService tokenService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new EntraExternalIdSSOProvider(tokenService);
    }

    @Test
    public void testAuthenticate_WithUsernamePassword() {
        AuthenticationResult result = provider.authenticate("test@example.com", "password");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("SSO_REDIRECT_REQUIRED"));
    }

    @Test
    public void testAuthenticateWithAuthorizationCode_Failure() {
        // Mock user profile extraction (won't be reached because token acquisition fails)
        ExternalIdTokenService.ExternalUserProfile profile =
                new ExternalIdTokenService.ExternalUserProfile("test@example.com", List.of("USER"));
        when(tokenService.getUserProfile(anyString())).thenReturn(profile);

        // Simulate MSAL acquiring token failure (future resolves to null)
        when(tokenService.acquireTokenByAuthorizationCode(anyString(), anyString(), anySet()))
                .thenReturn(CompletableFuture.completedFuture(null));

        AuthenticationResult result = provider.authenticateWithAuthorizationCode("auth-code", "http://localhost/callback");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("failed"));
    }

    @Test
    public void testAuthenticateWithAuthorizationCode_Exception() {
        when(tokenService.acquireTokenByAuthorizationCode(anyString(), anyString(), anySet()))
                .thenThrow(new RuntimeException("MSAL4J error"));
        AuthenticationResult result = provider.authenticateWithAuthorizationCode("bad", "http://localhost/callback");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().toLowerCase().contains("failed"));
    }

    @Test
    public void testValidateEntraToken_Success() {
        ExternalIdTokenService.ExternalUserProfile profile =
                new ExternalIdTokenService.ExternalUserProfile("test@example.com", List.of("ADMIN"));
        ExternalIdTokenService.ExternalTokenValidationResult validation =
                new ExternalIdTokenService.ExternalTokenValidationResult(true, "Valid", profile);
        when(tokenService.validateToken(anyString())).thenReturn(validation);

        AuthenticationResult result = provider.validateEntraToken("token");
        assertTrue(result.isSuccess());
        assertEquals("test@example.com", result.getUserId());
        assertTrue(result.getRoles().contains("ADMIN"));
    }

    @Test
    public void testValidateEntraToken_Invalid() {
        ExternalIdTokenService.ExternalTokenValidationResult validation =
                new ExternalIdTokenService.ExternalTokenValidationResult(false, "Invalid token", null);
        when(tokenService.validateToken(anyString())).thenReturn(validation);
        AuthenticationResult result = provider.validateEntraToken("bad");
        assertFalse(result.isSuccess());
        assertEquals("Invalid token", result.getMessage());
    }

    @Test
    public void testIsDomainEnabledForSSO() {
        List<String> domains = List.of("gmail.com", "microsoft.com");
        assertTrue(provider.isDomainEnabledForSSO("user@gmail.com", domains));
        assertFalse(provider.isDomainEnabledForSSO("user@other.com", domains));
    }

    @Test
    public void testSupports() {
        assertTrue(provider.supports("EntraExternalIdSSO"));
        assertFalse(provider.supports("Other"));
    }

    @Test
    public void testLogout() {
        assertDoesNotThrow(() -> provider.logout("session"));
    }
}
