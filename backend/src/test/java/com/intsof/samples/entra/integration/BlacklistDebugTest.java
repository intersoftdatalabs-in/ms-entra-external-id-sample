package com.intsof.samples.entra.integration;

import com.intsof.samples.entra.service.JwtService;
import com.intsof.samples.entra.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test for blacklist functionality
 */
public class BlacklistDebugTest {
    
    private JwtService jwtService;
    private TokenBlacklistService tokenBlacklistService;
    
    @BeforeEach
    public void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
        
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "issuer", "test-issuer");
        ReflectionTestUtils.setField(jwtService, "tokenBlacklistService", tokenBlacklistService);
    }
    
    @Test
    public void testDebugBlacklistWorkflow() {
        String username = "testuser";
        List<String> roles = List.of("USER");
        
        // Generate access token
        String accessToken = jwtService.generateToken(username, roles, null);
        System.out.println("Generated token: " + accessToken.substring(0, 20) + "...");
        
        // Check initial validation
        boolean initialValidation = jwtService.validateTokenWithBlacklist(accessToken);
        System.out.println("Initial validation: " + initialValidation);
        assertTrue(initialValidation);
        
        // Check if initially blacklisted
        boolean initiallyBlacklisted = tokenBlacklistService.isTokenBlacklisted(accessToken);
        System.out.println("Initially blacklisted: " + initiallyBlacklisted);
        assertFalse(initiallyBlacklisted);
        
        // Blacklist the token with future expiration
        java.util.Date futureExpiration = new java.util.Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        tokenBlacklistService.blacklistToken(accessToken, futureExpiration);
        System.out.println("Token blacklisted");
        
        // Check if now blacklisted
        boolean nowBlacklisted = tokenBlacklistService.isTokenBlacklisted(accessToken);
        System.out.println("Now blacklisted: " + nowBlacklisted);
        assertTrue(nowBlacklisted);
        
        // Check validation after blacklisting
        boolean validationAfterBlacklist = jwtService.validateTokenWithBlacklist(accessToken);
        System.out.println("Validation after blacklist: " + validationAfterBlacklist);
        
        // This should now be false
        assertFalse(validationAfterBlacklist);
    }
}
