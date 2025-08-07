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
 * Unit tests for {@link EntraExternalIdSSOProvider}.
 *
 * These tests focus on exercising the public contract of the provider while
 * stubbing out the lower-level {@link ExternalIdTokenService} dependency via
 * Mockito. Only lightweight domain objects defined inside the SPI are used, so
 * no real calls to Microsoft endpoints are required.
 */
public class EntraExternalIdSSOProviderTest {

    private EntraExternalIdSSOProvider provider;

    @Mock
    private ExternalIdTokenService externalIdTokenService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new EntraExternalIdSSOProvider(externalIdTokenService);
    }

    @Test
    public void testAuthenticate_WithUsernamePassword() {
        AuthenticationResult result = provider.authenticate("test@example.com", "password");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not supported"));
    }

    @Test
    public void testAuthenticateWithAuthorizationCode_Failure() {
        // Simulate failure scenario where MSAL4J returns null
        when(externalIdTokenService.acquireTokenByAuthorizationCode(anyString(), anyString(), anySet()))
                .thenReturn(CompletableFuture.completedFuture(null));

        AuthenticationResult result = provider.authenticateWithAuthorizationCode("auth-code", "http://localhost/callback");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Failed to acquire token"));
    }

    @Test
    public void testValidateEntraToken_Success() {
        ExternalIdTokenService.ExternalUserProfile mockProfile =
                new ExternalIdTokenService.ExternalUserProfile("test@example.com", List.of("ADMIN"));

        ExternalIdTokenService.ExternalTokenValidationResult mockValidationResult =
                new ExternalIdTokenService.ExternalTokenValidationResult(true, "Valid token", mockProfile);

        when(externalIdTokenService.validateToken(anyString())).thenReturn(mockValidationResult);

        AuthenticationResult result = provider.validateEntraToken("valid-token");

        assertTrue(result.isSuccess());
        assertEquals("test@example.com", result.getUserId());
        assertEquals("Token validation successful", result.getMessage());
        assertTrue(result.getRoles().contains("ADMIN"));
    }

    @Test
    public void testValidateEntraToken_Invalid() {
        ExternalIdTokenService.ExternalTokenValidationResult mockValidationResult =
                new ExternalIdTokenService.ExternalTokenValidationResult(false, "Invalid token", null);

        when(externalIdTokenService.validateToken(anyString())).thenReturn(mockValidationResult);

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
        // Ensure logout does not throw
        assertDoesNotThrow(() -> provider.logout("session-id"));
    }
}