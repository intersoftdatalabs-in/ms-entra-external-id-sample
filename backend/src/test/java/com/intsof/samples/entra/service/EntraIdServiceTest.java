package com.intsof.samples.entra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EntraIdService
 */
public class EntraIdServiceTest {
    
    private EntraIdService entraIdService;
    
    @BeforeEach
    public void setUp() {
        entraIdService = new EntraIdService();
        
        // Set test configuration
        ReflectionTestUtils.setField(entraIdService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(entraIdService, "tenantId", "test-tenant-id");
        ReflectionTestUtils.setField(entraIdService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(entraIdService, "authorizationUri", "https://login.microsoftonline.com/test-tenant/oauth2/v2.0/authorize");
        ReflectionTestUtils.setField(entraIdService, "tokenUri", "https://login.microsoftonline.com/test-tenant/oauth2/v2.0/token");
        ReflectionTestUtils.setField(entraIdService, "userInfoUri", "https://graph.microsoft.com/v1.0/me");
    }
    
    @Test
    public void testValidateToken_WithExpiredToken() {
        // Create an expired token
        String expiredToken = createTestJWT(System.currentTimeMillis() / 1000 - 100); // Expired 100 seconds ago
        
        EntraIdService.EntraTokenValidationResult result = entraIdService.validateToken(expiredToken);
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("expired"));
    }
    
    @Test
    public void testValidateToken_WithValidToken() {
        // Create a valid token with correct issuer and audience
        String validToken = createTestJWT(System.currentTimeMillis() / 1000 + 3600); // Expires in 1 hour
        
        EntraIdService.EntraTokenValidationResult result = entraIdService.validateToken(validToken);
        
        // This should pass validation since the token has valid issuer, audience, and expiration
        assertTrue(result.isValid());
        assertEquals("Token validation successful", result.getMessage());
        assertNotNull(result.getUserProfile());
    }
    
    @Test
    public void testValidateToken_WithInvalidIssuer() {
        // Create a token with invalid issuer
        String invalidIssuerToken = createTestJWTWithInvalidIssuer();
        
        EntraIdService.EntraTokenValidationResult result = entraIdService.validateToken(invalidIssuerToken);
        
        assertFalse(result.isValid());
        assertEquals("Invalid token issuer", result.getMessage());
    }
    
    @Test
    public void testValidateToken_WithInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        
        EntraIdService.EntraTokenValidationResult result = entraIdService.validateToken(invalidToken);
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("failed"));
    }
    
    @Test
    public void testGetUserProfile_WithValidToken() {
        String validToken = createTestJWTWithUserInfo();
        
        EntraIdService.EntraUserProfile profile = entraIdService.getUserProfile(validToken);
        
        assertNotNull(profile);
        assertEquals("test@example.com", profile.getEmail());
        assertEquals("Test User", profile.getName());
        assertEquals("Test", profile.getGivenName());
        assertEquals("User", profile.getFamilyName());
        assertTrue(profile.getRoles().contains("USER"));
    }
    
    @Test
    public void testEntraUserProfile_Creation() {
        EntraIdService.EntraUserProfile profile = new EntraIdService.EntraUserProfile(
            "test@example.com", 
            "Test User", 
            "Test", 
            "User", 
            "test-subject", 
            java.util.List.of("ADMIN", "USER"), 
            java.util.List.of("group1", "group2")
        );
        
        assertEquals("test@example.com", profile.getEmail());
        assertEquals("Test User", profile.getName());
        assertEquals("Test", profile.getGivenName());
        assertEquals("User", profile.getFamilyName());
        assertEquals("test-subject", profile.getSubject());
        assertEquals(2, profile.getRoles().size());
        assertTrue(profile.getRoles().contains("ADMIN"));
        assertTrue(profile.getRoles().contains("USER"));
        assertEquals(2, profile.getGroups().size());
        assertTrue(profile.getGroups().contains("group1"));
        assertTrue(profile.getGroups().contains("group2"));
    }
    
    @Test
    public void testEntraTokenValidationResult_Creation() {
        EntraIdService.EntraUserProfile profile = new EntraIdService.EntraUserProfile(
            "test@example.com", null, null, null, null, null, null
        );
        
        EntraIdService.EntraTokenValidationResult result = 
            new EntraIdService.EntraTokenValidationResult(true, "Success", profile);
        
        assertTrue(result.isValid());
        assertEquals("Success", result.getMessage());
        assertNotNull(result.getUserProfile());
        assertEquals("test@example.com", result.getUserProfile().getEmail());
    }
    
    /**
     * Create a test JWT token with expiration
     */
    private String createTestJWT(long expirationTime) {
        String header = java.util.Base64.getEncoder().encodeToString(
            "{\"typ\":\"JWT\",\"alg\":\"RS256\"}".getBytes()
        );
        
        String payload = java.util.Base64.getEncoder().encodeToString(
            String.format("{\"exp\":%d,\"iss\":\"https://login.microsoftonline.com/test-tenant\",\"aud\":\"test-client-id\"}", 
                expirationTime).getBytes()
        );
        
        String signature = java.util.Base64.getEncoder().encodeToString("fake-signature".getBytes());
        
        return header + "." + payload + "." + signature;
    }
    
    /**
     * Create a test JWT token with invalid issuer
     */
    private String createTestJWTWithInvalidIssuer() {
        String header = java.util.Base64.getEncoder().encodeToString(
            "{\"typ\":\"JWT\",\"alg\":\"RS256\"}".getBytes()
        );
        
        String payload = java.util.Base64.getEncoder().encodeToString(
            String.format("{\"exp\":%d,\"iss\":\"https://invalid-issuer.com\",\"aud\":\"test-client-id\"}", 
                System.currentTimeMillis() / 1000 + 3600).getBytes()
        );
        
        String signature = java.util.Base64.getEncoder().encodeToString("fake-signature".getBytes());
        
        return header + "." + payload + "." + signature;
    }
    
    /**
     * Create a test JWT token with user information
     */
    private String createTestJWTWithUserInfo() {
        String header = java.util.Base64.getEncoder().encodeToString(
            "{\"typ\":\"JWT\",\"alg\":\"RS256\"}".getBytes()
        );
        
        String payload = java.util.Base64.getEncoder().encodeToString(
            ("{\"exp\":" + (System.currentTimeMillis() / 1000 + 3600) + "," +
             "\"iss\":\"https://login.microsoftonline.com/test-tenant\"," +
             "\"aud\":\"test-client-id\"," +
             "\"email\":\"test@example.com\"," +
             "\"name\":\"Test User\"," +
             "\"given_name\":\"Test\"," +
             "\"family_name\":\"User\"," +
             "\"sub\":\"test-subject\"," +
             "\"roles\":[\"USER\"]}").getBytes()
        );
        
        String signature = java.util.Base64.getEncoder().encodeToString("fake-signature".getBytes());
        
        return header + "." + payload + "." + signature;
    }
}
