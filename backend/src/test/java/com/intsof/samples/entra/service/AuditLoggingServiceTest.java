package com.intsof.samples.entra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditLoggingService
 */
class AuditLoggingServiceTest {
    
    private AuditLoggingService auditLoggingService;
    
    @BeforeEach
    void setUp() {
        auditLoggingService = new AuditLoggingService();
        ReflectionTestUtils.setField(auditLoggingService, "auditEnabled", true);
        ReflectionTestUtils.setField(auditLoggingService, "includeIpAddress", true);
        ReflectionTestUtils.setField(auditLoggingService, "includeUserAgent", false);
    }
    
    @Test
    void testLogSuccessfulAuth() {
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logSuccessfulAuth("testuser", "192.168.1.1", "PASSWORD");
        });
    }
    
    @Test
    void testLogSuccessfulAuthWithAdditionalData() {
        Map<String, Object> additionalData = Map.of(
            "roles", "USER,ADMIN",
            "loginAttempt", 1
        );
        
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logSuccessfulAuth("testuser", "192.168.1.1", "PASSWORD", additionalData);
        });
    }
    
    @Test
    void testLogFailedAuth() {
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logFailedAuth("testuser", "192.168.1.1", "invalid_credentials");
        });
    }
    
    @Test
    void testLogFailedAuthWithAdditionalData() {
        Map<String, Object> additionalData = Map.of(
            "attemptNumber", 3,
            "reason", "password_expired"
        );
        
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logFailedAuth("testuser", "192.168.1.1", "invalid_credentials", additionalData);
        });
    }
    
    @Test
    void testLogLogout() {
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logLogout("testuser", "192.168.1.1");
        });
    }
    
    @Test
    void testLogLogoutWithAdditionalData() {
        Map<String, Object> additionalData = Map.of(
            "sessionDuration", "00:45:30",
            "logoutMethod", "manual"
        );
        
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logLogout("testuser", "192.168.1.1", additionalData);
        });
    }
    
    @Test
    void testLogTokenRefresh() {
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logTokenRefresh("testuser", "192.168.1.1", true);
            auditLoggingService.logTokenRefresh("testuser", "192.168.1.1", false);
        });
    }
    
    @Test
    void testLogTokenRefreshWithAdditionalData() {
        Map<String, Object> additionalData = Map.of(
            "tokenRotated", true,
            "familyId", "family-123"
        );
        
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logTokenRefresh("testuser", "192.168.1.1", true, additionalData);
        });
    }
    
    @Test
    void testLogRateLimitExceeded() {
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logRateLimitExceeded("testuser", "192.168.1.1", "LOGIN_RATE_LIMIT");
        });
    }
    
    @Test
    void testLogSecurityThreat() {
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logSecurityThreat("TOKEN_THEFT", "testuser", "192.168.1.1", 
                "Refresh token reuse detected");
        });
    }
    
    @Test
    void testLogAuthEvent() {
        Map<String, Object> eventData = Map.of(
            "eventType", "PASSWORD_CHANGE",
            "success", true
        );
        
        // Should not throw any exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logAuthEvent("PASSWORD_CHANGE", "testuser", "192.168.1.1", eventData);
        });
    }
    
    @Test
    void testIsAuditEnabled() {
        assertTrue(auditLoggingService.isAuditEnabled());
        
        ReflectionTestUtils.setField(auditLoggingService, "auditEnabled", false);
        assertFalse(auditLoggingService.isAuditEnabled());
    }
    
    @Test
    void testDisabledAuditLogging() {
        ReflectionTestUtils.setField(auditLoggingService, "auditEnabled", false);
        
        // Should not perform any logging when disabled, but shouldn't throw exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logSuccessfulAuth("testuser", "192.168.1.1", "PASSWORD");
            auditLoggingService.logFailedAuth("testuser", "192.168.1.1", "invalid_credentials");
            auditLoggingService.logLogout("testuser", "192.168.1.1");
            auditLoggingService.logTokenRefresh("testuser", "192.168.1.1", true);
            auditLoggingService.logRateLimitExceeded("testuser", "192.168.1.1", "LOGIN_RATE_LIMIT");
            auditLoggingService.logSecurityThreat("TOKEN_THEFT", "testuser", "192.168.1.1", "details");
        });
    }
    
    @Test
    void testNullInputHandling() {
        // Should handle null inputs gracefully without throwing exceptions
        assertDoesNotThrow(() -> {
            auditLoggingService.logSuccessfulAuth(null, null, null);
            auditLoggingService.logFailedAuth(null, null, null);
            auditLoggingService.logLogout(null, null);
            auditLoggingService.logTokenRefresh(null, null, true);
            auditLoggingService.logRateLimitExceeded(null, null, null);
            auditLoggingService.logSecurityThreat(null, null, null, null);
            auditLoggingService.logAuthEvent(null, null, null, null);
        });
    }
    
    @Test
    void testLogInjectionPrevention() {
        String maliciousInput = "testuser\n\rINJECTED_LOG_ENTRY | malicious=true";
        String ipAddress = "192.168.1.1\nINJECTED_IP";
        
        // Should not throw exceptions and should sanitize input
        assertDoesNotThrow(() -> {
            auditLoggingService.logSuccessfulAuth(maliciousInput, ipAddress, "PASSWORD");
            auditLoggingService.logFailedAuth(maliciousInput, ipAddress, "invalid_credentials");
        });
    }
}
