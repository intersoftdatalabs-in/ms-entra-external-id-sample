package com.intsof.samples.entra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting service for authentication endpoints
 * Supports both IP-based and user-based rate limiting
 */
@Service
public class RateLimitingService {
    
    @Value("${auth.rate-limit.max-attempts:5}")
    private int maxAttempts;
    
    @Value("${auth.rate-limit.window-minutes:1}")
    private int windowMinutes;
    
    @Value("${auth.rate-limit.enabled:true}")
    private boolean rateLimitingEnabled;
    
    // IP-based rate limiting
    private final ConcurrentHashMap<String, RateLimitCounter> ipAttempts = new ConcurrentHashMap<>();
    
    // User-based rate limiting  
    private final ConcurrentHashMap<String, RateLimitCounter> userAttempts = new ConcurrentHashMap<>();
    
    // Cleanup executor
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public RateLimitingService() {
        // Schedule cleanup task to run every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredCounters, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Check if an authentication attempt is allowed for the given IP
     */
    public boolean isIpAllowed(String ipAddress) {
        if (!rateLimitingEnabled || ipAddress == null) return true;
        
        RateLimitCounter counter = ipAttempts.computeIfAbsent(ipAddress, k -> new RateLimitCounter());
        return counter.isAllowed(maxAttempts, windowMinutes);
    }
    
    /**
     * Check if an authentication attempt is allowed for the given user
     */
    public boolean isUserAllowed(String username) {
        if (!rateLimitingEnabled || username == null) return true;
        
        RateLimitCounter counter = userAttempts.computeIfAbsent(username.toLowerCase(), k -> new RateLimitCounter());
        return counter.isAllowed(maxAttempts, windowMinutes);
    }
    
    /**
     * Check if both IP and user are allowed (combined check)
     */
    public boolean isAllowed(String ipAddress, String username) {
        return isIpAllowed(ipAddress) && isUserAllowed(username);
    }
    
    /**
     * Record a failed authentication attempt
     */
    public void recordFailedAttempt(String ipAddress, String username) {
        if (!rateLimitingEnabled) return;
        
        if (ipAddress != null) {
            RateLimitCounter ipCounter = ipAttempts.computeIfAbsent(ipAddress, k -> new RateLimitCounter());
            ipCounter.recordAttempt();
        }
        
        if (username != null) {
            RateLimitCounter userCounter = userAttempts.computeIfAbsent(username.toLowerCase(), k -> new RateLimitCounter());
            userCounter.recordAttempt();
        }
    }
    
    /**
     * Reset rate limit counters for successful authentication
     */
    public void resetCounters(String ipAddress, String username) {
        if (ipAddress != null) {
            ipAttempts.remove(ipAddress);
        }
        
        if (username != null) {
            userAttempts.remove(username.toLowerCase());
        }
    }
    
    /**
     * Get remaining attempts for IP
     */
    public int getRemainingAttempts(String ipAddress) {
        if (!rateLimitingEnabled || ipAddress == null) return maxAttempts;
        
        RateLimitCounter counter = ipAttempts.get(ipAddress);
        if (counter == null) return maxAttempts;
        
        return Math.max(0, maxAttempts - counter.getAttemptCount());
    }
    
    /**
     * Get time until next attempt is allowed (in seconds)
     */
    public long getTimeUntilNextAttempt(String ipAddress, String username) {
        if (!rateLimitingEnabled) return 0;
        
        long ipWaitTime = 0;
        long userWaitTime = 0;
        
        if (ipAddress != null) {
            RateLimitCounter ipCounter = ipAttempts.get(ipAddress);
            if (ipCounter != null) {
                ipWaitTime = ipCounter.getTimeUntilReset(windowMinutes);
            }
        }
        
        if (username != null) {
            RateLimitCounter userCounter = userAttempts.get(username.toLowerCase());
            if (userCounter != null) {
                userWaitTime = userCounter.getTimeUntilReset(windowMinutes);
            }
        }
        
        return Math.max(ipWaitTime, userWaitTime);
    }
    
    /**
     * Cleanup expired rate limit counters
     */
    private void cleanupExpiredCounters() {
        long now = System.currentTimeMillis();
        long expireTime = windowMinutes * 60 * 1000L;
        
        ipAttempts.entrySet().removeIf(entry -> 
            now - entry.getValue().getFirstAttemptTime() > expireTime);
            
        userAttempts.entrySet().removeIf(entry -> 
            now - entry.getValue().getFirstAttemptTime() > expireTime);
    }
    
    /**
     * Get current statistics for monitoring
     */
    public RateLimitStats getStats() {
        return new RateLimitStats(
            ipAttempts.size(),
            userAttempts.size(),
            maxAttempts,
            windowMinutes,
            rateLimitingEnabled
        );
    }
    
    /**
     * Shutdown the cleanup executor
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
    
    /**
     * Inner class to track rate limit attempts
     */
    private static class RateLimitCounter {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private volatile long firstAttemptTime = System.currentTimeMillis();
        
        public boolean isAllowed(int maxAttempts, int windowMinutes) {
            long now = System.currentTimeMillis();
            long windowMillis = windowMinutes * 60 * 1000L;
            
            // Reset counter if window has expired
            if (now - firstAttemptTime > windowMillis) {
                attempts.set(0);
                firstAttemptTime = now;
            }
            
            return attempts.get() < maxAttempts;
        }
        
        public void recordAttempt() {
            attempts.incrementAndGet();
        }
        
        public int getAttemptCount() {
            return attempts.get();
        }
        
        public long getFirstAttemptTime() {
            return firstAttemptTime;
        }
        
        public long getTimeUntilReset(int windowMinutes) {
            long now = System.currentTimeMillis();
            long windowMillis = windowMinutes * 60 * 1000L;
            long timeElapsed = now - firstAttemptTime;
            
            if (timeElapsed >= windowMillis) {
                return 0;
            }
            
            return (windowMillis - timeElapsed) / 1000; // Return seconds
        }
    }
    
    /**
     * Statistics class for monitoring
     */
    public static class RateLimitStats {
        private final int activeIpCounters;
        private final int activeUserCounters;
        private final int maxAttempts;
        private final int windowMinutes;
        private final boolean enabled;
        
        public RateLimitStats(int activeIpCounters, int activeUserCounters, 
                             int maxAttempts, int windowMinutes, boolean enabled) {
            this.activeIpCounters = activeIpCounters;
            this.activeUserCounters = activeUserCounters;
            this.maxAttempts = maxAttempts;
            this.windowMinutes = windowMinutes;
            this.enabled = enabled;
        }
        
        // Getters
        public int getActiveIpCounters() { return activeIpCounters; }
        public int getActiveUserCounters() { return activeUserCounters; }
        public int getMaxAttempts() { return maxAttempts; }
        public int getWindowMinutes() { return windowMinutes; }
        public boolean isEnabled() { return enabled; }
        
        @Override
        public String toString() {
            return "RateLimitStats{" +
                    "activeIpCounters=" + activeIpCounters +
                    ", activeUserCounters=" + activeUserCounters +
                    ", maxAttempts=" + maxAttempts +
                    ", windowMinutes=" + windowMinutes +
                    ", enabled=" + enabled +
                    '}';
        }
    }
}
