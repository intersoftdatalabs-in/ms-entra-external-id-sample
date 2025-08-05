package com.intsof.samples.entra.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RefreshTokenFamily
 */
class RefreshTokenFamilyTest {
    
    private RefreshTokenFamily tokenFamily;
    private final String familyId = "family-123";
    private final String username = "testuser";
    private final String initialToken = "initial.jwt.token";
    
    @BeforeEach
    void setUp() {
        tokenFamily = new RefreshTokenFamily(familyId, username, initialToken);
    }
    
    @Test
    void testConstructor() {
        assertEquals(familyId, tokenFamily.getFamilyId());
        assertEquals(username, tokenFamily.getUsername());
        assertEquals(initialToken, tokenFamily.getCurrentToken());
        assertFalse(tokenFamily.isCompromised());
        assertNotNull(tokenFamily.getCreatedAt());
        assertNotNull(tokenFamily.getLastUsed());
        assertEquals(1, tokenFamily.getTokenHistory().size());
        assertTrue(tokenFamily.getTokenHistory().contains(initialToken));
    }
    
    @Test
    void testRotateToken() {
        String newToken = "new.jwt.token";
        
        tokenFamily.rotateToken(newToken);
        
        assertEquals(newToken, tokenFamily.getCurrentToken());
        assertEquals(2, tokenFamily.getTokenHistory().size());
        assertTrue(tokenFamily.getTokenHistory().contains(initialToken));
        assertTrue(tokenFamily.getTokenHistory().contains(newToken));
    }
    
    @Test
    void testRotateTokenHistoryLimit() {
        // Add more than 10 tokens to test history limit
        for (int i = 1; i <= 12; i++) {
            tokenFamily.rotateToken("token" + i);
        }
        
        // Should keep only last 10 tokens
        assertEquals(10, tokenFamily.getTokenHistory().size());
        assertFalse(tokenFamily.getTokenHistory().contains(initialToken)); // First token should be removed
        assertTrue(tokenFamily.getTokenHistory().contains("token12")); // Latest token should be present
    }
    
    @Test
    void testContainsToken() {
        assertTrue(tokenFamily.containsToken(initialToken));
        assertFalse(tokenFamily.containsToken("non.existent.token"));
        
        String newToken = "new.jwt.token";
        tokenFamily.rotateToken(newToken);
        
        assertTrue(tokenFamily.containsToken(initialToken));
        assertTrue(tokenFamily.containsToken(newToken));
    }
    
    @Test
    void testMarkAsCompromised() {
        assertFalse(tokenFamily.isCompromised());
        
        tokenFamily.markAsCompromised();
        
        assertTrue(tokenFamily.isCompromised());
    }
    
    @Test
    void testIsValidForRefresh() {
        // Should be valid for current token when not compromised
        assertTrue(tokenFamily.isValidForRefresh(initialToken));
        
        // Should not be valid for non-current token
        String newToken = "new.jwt.token";
        tokenFamily.rotateToken(newToken);
        assertFalse(tokenFamily.isValidForRefresh(initialToken));
        assertTrue(tokenFamily.isValidForRefresh(newToken));
        
        // Should not be valid when compromised
        tokenFamily.markAsCompromised();
        assertFalse(tokenFamily.isValidForRefresh(newToken));
    }
    
    @Test
    void testIsTokenReuse() {
        String newToken = "new.jwt.token";
        tokenFamily.rotateToken(newToken);
        
        // Using old token should be detected as reuse
        assertTrue(tokenFamily.isTokenReuse(initialToken));
        
        // Using current token should not be reuse
        assertFalse(tokenFamily.isTokenReuse(newToken));
        
        // Using non-existent token should not be reuse
        assertFalse(tokenFamily.isTokenReuse("non.existent.token"));
    }
    
    @Test
    void testGetAllTokens() {
        String token1 = "token1";
        String token2 = "token2";
        
        tokenFamily.rotateToken(token1);
        tokenFamily.rotateToken(token2);
        
        List<String> allTokens = tokenFamily.getAllTokens();
        assertEquals(3, allTokens.size());
        assertTrue(allTokens.contains(initialToken));
        assertTrue(allTokens.contains(token1));
        assertTrue(allTokens.contains(token2));
        
        // Should return a copy, not the original list
        allTokens.clear();
        assertEquals(3, tokenFamily.getTokenHistory().size());
    }
    
    @Test
    void testGetTokenHistory() {
        String newToken = "new.jwt.token";
        tokenFamily.rotateToken(newToken);
        
        List<String> history = tokenFamily.getTokenHistory();
        assertEquals(2, history.size());
        
        // Should return a copy, not the original list
        history.clear();
        assertEquals(2, tokenFamily.getTokenHistory().size());
    }
    
    @Test
    void testToString() {
        String stringRepresentation = tokenFamily.toString();
        
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains(familyId));
        assertTrue(stringRepresentation.contains(username));
        assertTrue(stringRepresentation.contains("tokenCount=1"));
        assertTrue(stringRepresentation.contains("isCompromised=false"));
    }
    
    @Test
    void testLastUsedUpdateOnRotation() throws InterruptedException {
        long initialTime = tokenFamily.getLastUsed().getTime();
        
        // Wait a bit to ensure time difference
        Thread.sleep(10);
        
        tokenFamily.rotateToken("new.token");
        
        long updatedTime = tokenFamily.getLastUsed().getTime();
        assertTrue(updatedTime > initialTime);
    }
}
