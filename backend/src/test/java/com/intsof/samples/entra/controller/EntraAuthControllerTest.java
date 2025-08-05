package com.intsof.samples.entra.controller;

import com.intsof.samples.entra.service.JwtService;
import com.intsof.samples.security.AuthenticationResult;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EntraAuthController
 */
public class EntraAuthControllerTest {
    
    private EntraAuthController controller;
    
    @Mock
    private EntraExternalIdSSOProvider entraProvider;
    
    @Mock
    private JwtService jwtService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EntraAuthController();
        
        // Inject mocks
        try {
            java.lang.reflect.Field providerField = EntraAuthController.class.getDeclaredField("entraProvider");
            providerField.setAccessible(true);
            providerField.set(controller, entraProvider);
            
            java.lang.reflect.Field jwtField = EntraAuthController.class.getDeclaredField("jwtService");
            jwtField.setAccessible(true);
            jwtField.set(controller, jwtService);
            
            // Inject configuration values
            java.lang.reflect.Field clientIdField = EntraAuthController.class.getDeclaredField("clientId");
            clientIdField.setAccessible(true);
            clientIdField.set(controller, "test-client-id");
            
            java.lang.reflect.Field tenantIdField = EntraAuthController.class.getDeclaredField("tenantId");
            tenantIdField.setAccessible(true);
            tenantIdField.set(controller, "test-tenant-id");
            
            java.lang.reflect.Field authUriField = EntraAuthController.class.getDeclaredField("authorizationUri");
            authUriField.setAccessible(true);
            authUriField.set(controller, "https://login.microsoftonline.com/");
        } catch (Exception e) {
            fail("Failed to inject mocks");
        }
    }
    
    @Test
    public void testHandleOAuthCallback_Success() {
        // Mock successful authentication
        AuthenticationResult successResult = new AuthenticationResult(
            true, "test@example.com", "Authentication successful", 
            "entra-access-token", "entra-refresh-token", 3600, List.of("USER")
        );
        
        when(entraProvider.authenticateWithAuthorizationCode(anyString(), anyString()))
            .thenReturn(successResult);
        
        when(jwtService.generateToken(anyString(), anyList(), isNull()))
            .thenReturn("app-access-token");
        
        when(jwtService.generateRefreshToken(anyString()))
            .thenReturn("app-refresh-token");
        
        ResponseEntity<?> response = controller.handleOAuthCallback("auth-code", "http://localhost/callback");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void testHandleOAuthCallback_AuthenticationFailure() {
        // Mock failed authentication
        AuthenticationResult failureResult = new AuthenticationResult(
            false, null, "Authentication failed"
        );
        
        when(entraProvider.authenticateWithAuthorizationCode(anyString(), anyString()))
            .thenReturn(failureResult);
        
        ResponseEntity<?> response = controller.handleOAuthCallback("invalid-code", "http://localhost/callback");
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Authentication failed", errorBody.get("error"));
    }
    
    @Test
    public void testHandleOAuthCallback_Exception() {
        // Mock exception during authentication
        when(entraProvider.authenticateWithAuthorizationCode(anyString(), anyString()))
            .thenThrow(new RuntimeException("MSAL4J error"));
        
        ResponseEntity<?> response = controller.handleOAuthCallback("auth-code", "http://localhost/callback");
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertTrue(errorBody.get("error").contains("OAuth callback processing failed"));
    }
    
    @Test
    public void testValidateEntraToken_Success() {
        // Mock successful token validation
        AuthenticationResult successResult = new AuthenticationResult(
            true, "test@example.com", "Token validation successful", 
            "entra-token", "entra-token", 0, List.of("ADMIN")
        );
        
        when(entraProvider.validateEntraToken(anyString()))
            .thenReturn(successResult);
        
        when(jwtService.generateToken(anyString(), anyList(), isNull()))
            .thenReturn("app-access-token");
        
        when(jwtService.generateRefreshToken(anyString()))
            .thenReturn("app-refresh-token");
        
        ResponseEntity<?> response = controller.validateEntraToken("Bearer valid-token");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void testValidateEntraToken_InvalidHeader() {
        ResponseEntity<?> response = controller.validateEntraToken("Invalid header");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertTrue(errorBody.get("error").contains("Missing or invalid Authorization header"));
    }
    
    @Test
    public void testValidateEntraToken_MissingHeader() {
        ResponseEntity<?> response = controller.validateEntraToken(null);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertTrue(errorBody.get("error").contains("Missing or invalid Authorization header"));
    }
    
    @Test
    public void testValidateEntraToken_ValidationFailure() {
        // Mock failed token validation
        AuthenticationResult failureResult = new AuthenticationResult(
            false, null, "Invalid token"
        );
        
        when(entraProvider.validateEntraToken(anyString()))
            .thenReturn(failureResult);
        
        ResponseEntity<?> response = controller.validateEntraToken("Bearer invalid-token");
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Invalid token", errorBody.get("error"));
    }
    
    @Test
    public void testGetAuthorizationUrl_Success() {
        ResponseEntity<?> response = controller.getAuthorizationUrl("http://localhost/callback", "state123");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody.get("authorization_url"));
        assertTrue(responseBody.get("authorization_url").contains("oauth2/v2.0/authorize"));
    }
    
    @Test
    public void testGetAuthorizationUrl_WithoutState() {
        ResponseEntity<?> response = controller.getAuthorizationUrl("http://localhost/callback", null);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody.get("authorization_url"));
        // Should not contain state parameter when null
        assertFalse(responseBody.get("authorization_url").contains("state="));
    }
}
