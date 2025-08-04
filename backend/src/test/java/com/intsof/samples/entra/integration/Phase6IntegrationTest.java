package com.intsof.samples.entra.integration;

import com.intsof.samples.entra.service.JwtService;
import com.intsof.samples.entra.service.RateLimitingService;
import com.intsof.samples.entra.service.AuditLoggingService;
import com.intsof.samples.entra.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Phase 6 advanced features
 */
public class Phase6IntegrationTest {
    
    private JwtService jwtService;
    private RateLimitingService rateLimitingService;
    private AuditLoggingService auditLoggingService;
    private TokenBlacklistService tokenBlacklistService;
    
    @BeforeEach
    public void setUp() {
        // Initialize services
        tokenBlacklistService = new TokenBlacklistService();
        
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "issuer", "test-issuer");
        ReflectionTestUtils.setField(jwtService, "tokenBlacklistService", tokenBlacklistService);
        
        rateLimitingService = new RateLimitingService();
        ReflectionTestUtils.setField(rateLimitingService, "maxAttempts", 3);
        ReflectionTestUtils.setField(rateLimitingService, "windowMinutes", 1);
        ReflectionTestUtils.setField(rateLimitingService, "rateLimitingEnabled", true);
        
        auditLoggingService = new AuditLoggingService();
    }
    
    @Test
    public void testTokenRefreshStrategyWithRotation() {
        String username = "testuser";
        
        // Generate initial refresh token
        String refreshToken1 = jwtService.generateRefreshToken(username);
        assertNotNull(refreshToken1);
        assertTrue(jwtService.validateToken(refreshToken1));
        
        // Generate new refresh token (simulating rotation)
        String refreshToken2 = jwtService.generateRefreshToken(username);
        assertNotNull(refreshToken2);
        assertNotEquals(refreshToken1, refreshToken2);
        
        // Both should be valid initially
        assertTrue(jwtService.validateToken(refreshToken1));
        assertTrue(jwtService.validateToken(refreshToken2));
    }
    
    @Test
    public void testTokenBlacklistingWorkflow() {
        String username = "testuser";
        List<String> roles = List.of("USER");
        
        // Generate access token
        String accessToken = jwtService.generateToken(username, roles, null);
        assertTrue(jwtService.validateTokenWithBlacklist(accessToken));
        
        // Blacklist the token with future expiration
        java.util.Date futureExpiration = new java.util.Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        tokenBlacklistService.blacklistToken(accessToken, futureExpiration);
        
        // Token should now be invalid
        assertFalse(jwtService.validateTokenWithBlacklist(accessToken));
        assertTrue(tokenBlacklistService.isTokenBlacklisted(accessToken));
    }
    
    @Test
    public void testRateLimitingBehavior() {
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        // Should allow first 3 attempts
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        
        // 4th attempt should be blocked
        assertFalse(rateLimitingService.isAllowed(ipAddress, username));
    }
    
    @Test
    public void testAuditLoggingFunctionality() {
        String username = "testuser";
        String ipAddress = "192.168.1.1";
        
        // Test that audit logging doesn't throw exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logSuccessfulAuth(username, ipAddress, "PASSWORD");
            auditLoggingService.logFailedAuth(username, ipAddress, "Invalid credentials");
            auditLoggingService.logLogout(username, ipAddress);
        });
    }
    
    @Test
    public void testCompleteAuthenticationWorkflow() {
        String username = "testuser";
        String ipAddress = "192.168.1.1";
        List<String> roles = List.of("USER", "ADMIN");
        
        // Check rate limiting allows request
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
        
        // Generate tokens
        String accessToken = jwtService.generateToken(username, roles, null);
        String refreshToken = jwtService.generateRefreshToken(username);
        
        // Validate tokens
        assertTrue(jwtService.validateTokenWithBlacklist(accessToken));
        assertTrue(jwtService.validateToken(refreshToken));
        
        // Log successful authentication
        auditLoggingService.logSuccessfulAuth(username, ipAddress, "PASSWORD");
        
        // Simulate logout - blacklist tokens with future expiration
        java.util.Date futureExpiration = new java.util.Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        tokenBlacklistService.blacklistToken(accessToken, futureExpiration);
        tokenBlacklistService.blacklistToken(refreshToken, futureExpiration);
        
        // Tokens should now be invalid
        assertFalse(jwtService.validateTokenWithBlacklist(accessToken));
        assertFalse(jwtService.validateTokenWithBlacklist(refreshToken));
        
        // Log logout
        auditLoggingService.logLogout(username, ipAddress);
    }
}
