package com.intsof.samples.entra.service;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory token blacklist service for logout token invalidation
 * and revoked token tracking
 */
@Service
public class TokenBlacklistService {
    
    // Map to store blacklisted tokens with their expiration times
    private final ConcurrentHashMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();
    
    // Scheduled executor for cleanup tasks
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public TokenBlacklistService() {
        // Schedule cleanup task to run every hour to remove expired blacklisted tokens
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * Add a token to the blacklist
     */
    public void blacklistToken(String token, Date expirationTime) {
        if (token != null && expirationTime != null && expirationTime.after(new Date())) {
            blacklistedTokens.put(token, expirationTime);
        }
    }
    
    /**
     * Check if a token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null) return true;
        
        Date expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) return false;
        
        // If token has expired, remove it from blacklist and return false
        if (expirationTime.before(new Date())) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * Remove a token from the blacklist (if needed)
     */
    public void removeToken(String token) {
        if (token != null) {
            blacklistedTokens.remove(token);
        }
    }
    
    /**
     * Blacklist all tokens in a refresh token family (for token theft detection)
     */
    public void blacklistTokenFamily(List<String> tokens, Date expirationTime) {
        if (tokens != null && expirationTime != null) {
            for (String token : tokens) {
                blacklistToken(token, expirationTime);
            }
        }
    }
    
    /**
     * Get the number of blacklisted tokens (for monitoring)
     */
    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }
    
    /**
     * Cleanup expired tokens from the blacklist
     */
    private void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
    
    /**
     * Shutdown the cleanup executor (for proper cleanup)
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
