package com.intsof.samples.entra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenBlacklistService
 */
class TokenBlacklistServiceTest {
    
    private TokenBlacklistService tokenBlacklistService;
    
    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }
    
    @Test
    void testBlacklistToken() {
        String token = "test.jwt.token";
        Date expirationTime = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        
        tokenBlacklistService.blacklistToken(token, expirationTime);
        
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void testTokenNotBlacklisted() {
        String token = "not.blacklisted.token";
        
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void testExpiredTokenAutoRemoval() {
        String token = "expired.jwt.token";
        Date expiredTime = new Date(System.currentTimeMillis() - 1000); // 1 second ago
        
        tokenBlacklistService.blacklistToken(token, expiredTime);
        
        // Token should be considered expired and automatically removed
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void testBlacklistTokenFamily() {
        List<String> tokens = List.of("token1", "token2", "token3");
        Date expirationTime = new Date(System.currentTimeMillis() + 3600000);
        
        tokenBlacklistService.blacklistTokenFamily(tokens, expirationTime);
        
        for (String token : tokens) {
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        }
    }
    
    @Test
    void testRemoveToken() {
        String token = "removable.jwt.token";
        Date expirationTime = new Date(System.currentTimeMillis() + 3600000);
        
        tokenBlacklistService.blacklistToken(token, expirationTime);
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        
        tokenBlacklistService.removeToken(token);
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }
    
    @Test
    void testGetBlacklistedTokenCount() {
        String token1 = "token1";
        String token2 = "token2";
        Date expirationTime = new Date(System.currentTimeMillis() + 3600000);
        
        assertEquals(0, tokenBlacklistService.getBlacklistedTokenCount());
        
        tokenBlacklistService.blacklistToken(token1, expirationTime);
        assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
        
        tokenBlacklistService.blacklistToken(token2, expirationTime);
        assertEquals(2, tokenBlacklistService.getBlacklistedTokenCount());
    }
    
    @Test
    void testNullTokenHandling() {
        Date expirationTime = new Date(System.currentTimeMillis() + 3600000);
        
        // Should not throw exception
        tokenBlacklistService.blacklistToken(null, expirationTime);
        assertTrue(tokenBlacklistService.isTokenBlacklisted(null));
        
        tokenBlacklistService.removeToken(null);
    }
    
    @Test
    void testNullExpirationHandling() {
        String token = "test.token";
        
        // Should not throw exception
        tokenBlacklistService.blacklistToken(token, null);
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }
}
