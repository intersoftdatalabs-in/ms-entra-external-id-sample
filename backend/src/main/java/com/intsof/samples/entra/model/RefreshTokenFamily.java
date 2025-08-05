package com.intsof.samples.entra.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a family of refresh tokens for tracking token rotation
 * and detecting potential token theft
 */
public class RefreshTokenFamily {
    private final String familyId;
    private final String username;
    private final List<String> tokenHistory;
    private String currentToken;
    private Date createdAt;
    private Date lastUsed;
    private boolean isCompromised;
    
    public RefreshTokenFamily(String familyId, String username, String initialToken) {
        this.familyId = familyId;
        this.username = username;
        this.currentToken = initialToken;
        this.tokenHistory = new ArrayList<>();
        this.tokenHistory.add(initialToken);
        this.createdAt = new Date();
        this.lastUsed = new Date();
        this.isCompromised = false;
    }
    
    /**
     * Rotate to a new refresh token
     */
    public void rotateToken(String newToken) {
        this.tokenHistory.add(newToken);
        this.currentToken = newToken;
        this.lastUsed = new Date();
        
        // Keep only last 10 tokens in history to prevent memory leaks
        if (tokenHistory.size() > 10) {
            tokenHistory.remove(0);
        }
    }
    
    /**
     * Check if a token belongs to this family
     */
    public boolean containsToken(String token) {
        return tokenHistory.contains(token);
    }
    
    /**
     * Mark the family as compromised (for token theft detection)
     */
    public void markAsCompromised() {
        this.isCompromised = true;
    }
    
    /**
     * Check if this family is still valid for refresh operations
     */
    public boolean isValidForRefresh(String token) {
        return !isCompromised && currentToken.equals(token);
    }
    
    /**
     * Check if a token is from this family but not the current token
     * (indicates potential token theft)
     */
    public boolean isTokenReuse(String token) {
        return containsToken(token) && !currentToken.equals(token);
    }
    
    // Getters
    public String getFamilyId() { return familyId; }
    public String getUsername() { return username; }
    public String getCurrentToken() { return currentToken; }
    public List<String> getTokenHistory() { return new ArrayList<>(tokenHistory); }
    public Date getCreatedAt() { return createdAt; }
    public Date getLastUsed() { return lastUsed; }
    public boolean isCompromised() { return isCompromised; }
    
    /**
     * Get all tokens in this family for blacklisting
     */
    public List<String> getAllTokens() {
        return new ArrayList<>(tokenHistory);
    }
    
    @Override
    public String toString() {
        return "RefreshTokenFamily{" +
                "familyId='" + familyId + '\'' +
                ", username='" + username + '\'' +
                ", tokenCount=" + tokenHistory.size() +
                ", isCompromised=" + isCompromised +
                ", lastUsed=" + lastUsed +
                '}';
    }
}
