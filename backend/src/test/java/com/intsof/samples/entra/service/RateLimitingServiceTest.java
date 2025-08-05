package com.intsof.samples.entra.service;

import com.intsof.samples.entra.service.RateLimitingService.RateLimitStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitingService
 */
class RateLimitingServiceTest {
    
    private RateLimitingService rateLimitingService;
    
    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
        // Set test configuration
        ReflectionTestUtils.setField(rateLimitingService, "maxAttempts", 3);
        ReflectionTestUtils.setField(rateLimitingService, "windowMinutes", 1);
        ReflectionTestUtils.setField(rateLimitingService, "rateLimitingEnabled", true);
    }
    
    @Test
    void testIpAllowedInitially() {
        String ipAddress = "192.168.1.1";
        assertTrue(rateLimitingService.isIpAllowed(ipAddress));
    }
    
    @Test
    void testUserAllowedInitially() {
        String username = "testuser";
        assertTrue(rateLimitingService.isUserAllowed(username));
    }
    
    @Test
    void testRecordFailedAttemptIp() {
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        // Should be allowed initially
        assertTrue(rateLimitingService.isIpAllowed(ipAddress));
        
        // Record attempts up to the limit - 1
        for (int i = 0; i < 2; i++) {
            rateLimitingService.recordFailedAttempt(ipAddress, username);
        }
        
        // Should still be allowed (2 attempts < 3 limit)
        assertTrue(rateLimitingService.isIpAllowed(ipAddress));
        
        // One more attempt should reach the limit
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        assertFalse(rateLimitingService.isIpAllowed(ipAddress));
    }
    
    @Test
    void testRecordFailedAttemptUser() {
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        // Should be allowed initially
        assertTrue(rateLimitingService.isUserAllowed(username));
        
        // Record attempts up to the limit - 1
        for (int i = 0; i < 2; i++) {
            rateLimitingService.recordFailedAttempt(ipAddress, username);
        }
        
        // Should still be allowed (2 attempts < 3 limit)
        assertTrue(rateLimitingService.isUserAllowed(username));
        
        // One more attempt should reach the limit
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        assertFalse(rateLimitingService.isUserAllowed(username));
    }
    
    @Test
    void testCombinedCheck() {
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
        
        // Exceed limits (3 attempts to reach the limit)
        for (int i = 0; i < 3; i++) {
            rateLimitingService.recordFailedAttempt(ipAddress, username);
        }
        
        assertFalse(rateLimitingService.isAllowed(ipAddress, username));
    }
    
    @Test
    void testResetCounters() {
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        // Exceed limits (3 attempts to reach limit)
        for (int i = 0; i < 3; i++) {
            rateLimitingService.recordFailedAttempt(ipAddress, username);
        }
        
        assertFalse(rateLimitingService.isAllowed(ipAddress, username));
        
        // Reset counters
        rateLimitingService.resetCounters(ipAddress, username);
        
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
    }
    
    @Test
    void testGetRemainingAttempts() {
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        assertEquals(3, rateLimitingService.getRemainingAttempts(ipAddress));
        
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        assertEquals(2, rateLimitingService.getRemainingAttempts(ipAddress));
        
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        assertEquals(1, rateLimitingService.getRemainingAttempts(ipAddress));
        
        rateLimitingService.recordFailedAttempt(ipAddress, username);
        assertEquals(0, rateLimitingService.getRemainingAttempts(ipAddress));
    }
    
    @Test
    void testGetTimeUntilNextAttempt() {
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        // Initially should be 0
        assertEquals(0, rateLimitingService.getTimeUntilNextAttempt(ipAddress, username));
        
        // After exceeding limit, should return positive time
        for (int i = 0; i < 3; i++) {
            rateLimitingService.recordFailedAttempt(ipAddress, username);
        }
        
        long timeUntilNext = rateLimitingService.getTimeUntilNextAttempt(ipAddress, username);
        assertTrue(timeUntilNext > 0);
        assertTrue(timeUntilNext <= 60); // Should be within 1 minute window
    }
    
    @Test
    void testGetStats() {
        String ipAddress1 = "192.168.1.1";
        String ipAddress2 = "192.168.1.2";
        String username1 = "user1";
        String username2 = "user2";
        
        RateLimitStats initialStats = rateLimitingService.getStats();
        assertEquals(0, initialStats.getActiveIpCounters());
        assertEquals(0, initialStats.getActiveUserCounters());
        assertEquals(3, initialStats.getMaxAttempts());
        assertEquals(1, initialStats.getWindowMinutes());
        assertTrue(initialStats.isEnabled());
        
        // Create some activity
        rateLimitingService.recordFailedAttempt(ipAddress1, username1);
        rateLimitingService.recordFailedAttempt(ipAddress2, username2);
        
        RateLimitStats activeStats = rateLimitingService.getStats();
        assertEquals(2, activeStats.getActiveIpCounters());
        assertEquals(2, activeStats.getActiveUserCounters());
    }
    
    @Test
    void testDisabledRateLimiting() {
        ReflectionTestUtils.setField(rateLimitingService, "rateLimitingEnabled", false);
        
        String ipAddress = "192.168.1.1";
        String username = "testuser";
        
        // Should always be allowed when disabled
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
        
        // Record many failed attempts
        for (int i = 0; i < 10; i++) {
            rateLimitingService.recordFailedAttempt(ipAddress, username);
        }
        
        // Should still be allowed
        assertTrue(rateLimitingService.isAllowed(ipAddress, username));
    }
    
    @Test
    void testNullInputHandling() {
        // Should handle null inputs gracefully
        assertTrue(rateLimitingService.isIpAllowed(null));
        assertTrue(rateLimitingService.isUserAllowed(null));
        assertTrue(rateLimitingService.isAllowed(null, null));
        
        // Should not throw exceptions
        rateLimitingService.recordFailedAttempt(null, null);
        rateLimitingService.resetCounters(null, null);
        
        assertEquals(3, rateLimitingService.getRemainingAttempts(null));
        assertEquals(0, rateLimitingService.getTimeUntilNextAttempt(null, null));
    }
}
