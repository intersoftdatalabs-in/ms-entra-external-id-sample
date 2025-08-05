package com.intsof.samples.entra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Audit logging service for authentication events
 * Logs to file system for security compliance and monitoring
 */
@Service
public class AuditLoggingService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Value("${audit.logging.enabled:true}")
    private boolean auditEnabled;
    
    @Value("${audit.logging.include-ip:true}")
    private boolean includeIpAddress;
    
    @Value("${audit.logging.include-user-agent:false}")
    private boolean includeUserAgent;
    
    /**
     * Log a successful authentication event
     */
    public void logSuccessfulAuth(String username, String ipAddress, String authMethod) {
        logSuccessfulAuth(username, ipAddress, authMethod, null);
    }
    
    /**
     * Log a successful authentication event with additional details
     */
    public void logSuccessfulAuth(String username, String ipAddress, String authMethod, Map<String, Object> additionalData) {
        if (!auditEnabled) return;
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("AUTH_SUCCESS | ");
        logMessage.append("timestamp=").append(LocalDateTime.now().format(DATE_FORMATTER)).append(" | ");
        logMessage.append("username=").append(sanitizeForLog(username)).append(" | ");
        logMessage.append("method=").append(sanitizeForLog(authMethod)).append(" | ");
        
        if (includeIpAddress && ipAddress != null) {
            logMessage.append("ip=").append(sanitizeForLog(ipAddress)).append(" | ");
        }
        
        if (additionalData != null) {
            additionalData.forEach((key, value) -> 
                logMessage.append(key).append("=").append(sanitizeForLog(String.valueOf(value))).append(" | "));
        }
        
        auditLogger.info(logMessage.toString());
    }
    
    /**
     * Log a failed authentication event
     */
    public void logFailedAuth(String username, String ipAddress, String reason) {
        logFailedAuth(username, ipAddress, reason, null);
    }
    
    /**
     * Log a failed authentication event with additional details
     */
    public void logFailedAuth(String username, String ipAddress, String reason, Map<String, Object> additionalData) {
        if (!auditEnabled) return;
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("AUTH_FAILURE | ");
        logMessage.append("timestamp=").append(LocalDateTime.now().format(DATE_FORMATTER)).append(" | ");
        logMessage.append("username=").append(sanitizeForLog(username)).append(" | ");
        logMessage.append("reason=").append(sanitizeForLog(reason)).append(" | ");
        
        if (includeIpAddress && ipAddress != null) {
            logMessage.append("ip=").append(sanitizeForLog(ipAddress)).append(" | ");
        }
        
        if (additionalData != null) {
            additionalData.forEach((key, value) -> 
                logMessage.append(key).append("=").append(sanitizeForLog(String.valueOf(value))).append(" | "));
        }
        
        auditLogger.warn(logMessage.toString());
    }
    
    /**
     * Log a logout event
     */
    public void logLogout(String username, String ipAddress) {
        logLogout(username, ipAddress, null);
    }
    
    /**
     * Log a logout event with additional details
     */
    public void logLogout(String username, String ipAddress, Map<String, Object> additionalData) {
        if (!auditEnabled) return;
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("LOGOUT | ");
        logMessage.append("timestamp=").append(LocalDateTime.now().format(DATE_FORMATTER)).append(" | ");
        logMessage.append("username=").append(sanitizeForLog(username)).append(" | ");
        
        if (includeIpAddress && ipAddress != null) {
            logMessage.append("ip=").append(sanitizeForLog(ipAddress)).append(" | ");
        }
        
        if (additionalData != null) {
            additionalData.forEach((key, value) -> 
                logMessage.append(key).append("=").append(sanitizeForLog(String.valueOf(value))).append(" | "));
        }
        
        auditLogger.info(logMessage.toString());
    }
    
    /**
     * Log token refresh events
     */
    public void logTokenRefresh(String username, String ipAddress, boolean success) {
        logTokenRefresh(username, ipAddress, success, null);
    }
    
    /**
     * Log token refresh events with additional details
     */
    public void logTokenRefresh(String username, String ipAddress, boolean success, Map<String, Object> additionalData) {
        if (!auditEnabled) return;
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(success ? "TOKEN_REFRESH_SUCCESS" : "TOKEN_REFRESH_FAILURE").append(" | ");
        logMessage.append("timestamp=").append(LocalDateTime.now().format(DATE_FORMATTER)).append(" | ");
        logMessage.append("username=").append(sanitizeForLog(username)).append(" | ");
        
        if (includeIpAddress && ipAddress != null) {
            logMessage.append("ip=").append(sanitizeForLog(ipAddress)).append(" | ");
        }
        
        if (additionalData != null) {
            additionalData.forEach((key, value) -> 
                logMessage.append(key).append("=").append(sanitizeForLog(String.valueOf(value))).append(" | "));
        }
        
        if (success) {
            auditLogger.info(logMessage.toString());
        } else {
            auditLogger.warn(logMessage.toString());
        }
    }
    
    /**
     * Log rate limiting events
     */
    public void logRateLimitExceeded(String username, String ipAddress, String limitType) {
        if (!auditEnabled) return;
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("RATE_LIMIT_EXCEEDED | ");
        logMessage.append("timestamp=").append(LocalDateTime.now().format(DATE_FORMATTER)).append(" | ");
        logMessage.append("username=").append(sanitizeForLog(username)).append(" | ");
        logMessage.append("limitType=").append(sanitizeForLog(limitType)).append(" | ");
        
        if (includeIpAddress && ipAddress != null) {
            logMessage.append("ip=").append(sanitizeForLog(ipAddress)).append(" | ");
        }
        
        auditLogger.warn(logMessage.toString());
    }
    
    /**
     * Log potential security threats (token theft, etc.)
     */
    public void logSecurityThreat(String threatType, String username, String ipAddress, String details) {
        if (!auditEnabled) return;
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("SECURITY_THREAT | ");
        logMessage.append("timestamp=").append(LocalDateTime.now().format(DATE_FORMATTER)).append(" | ");
        logMessage.append("threatType=").append(sanitizeForLog(threatType)).append(" | ");
        logMessage.append("username=").append(sanitizeForLog(username)).append(" | ");
        logMessage.append("details=").append(sanitizeForLog(details)).append(" | ");
        
        if (includeIpAddress && ipAddress != null) {
            logMessage.append("ip=").append(sanitizeForLog(ipAddress)).append(" | ");
        }
        
        auditLogger.error(logMessage.toString());
    }
    
    /**
     * Log general authentication events
     */
    public void logAuthEvent(String eventType, String username, String ipAddress, Map<String, Object> eventData) {
        if (!auditEnabled) return;
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("AUTH_EVENT | ");
        logMessage.append("timestamp=").append(LocalDateTime.now().format(DATE_FORMATTER)).append(" | ");
        logMessage.append("eventType=").append(sanitizeForLog(eventType)).append(" | ");
        logMessage.append("username=").append(sanitizeForLog(username)).append(" | ");
        
        if (includeIpAddress && ipAddress != null) {
            logMessage.append("ip=").append(sanitizeForLog(ipAddress)).append(" | ");
        }
        
        if (eventData != null) {
            eventData.forEach((key, value) -> 
                logMessage.append(key).append("=").append(sanitizeForLog(String.valueOf(value))).append(" | "));
        }
        
        auditLogger.info(logMessage.toString());
    }
    
    /**
     * Sanitize log input to prevent log injection attacks
     */
    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        
        // Remove line breaks and control characters that could be used for log injection
        return input.replaceAll("[\r\n\t]", "_")
                   .replaceAll("[\\p{Cntrl}]", "")
                   .trim();
    }
    
    /**
     * Check if audit logging is enabled
     */
    public boolean isAuditEnabled() {
        return auditEnabled;
    }
}
