package com.intsof.samples.entra.controller;

import com.intsof.samples.entra.dto.LoginRequest;
import com.intsof.samples.entra.dto.TokenResponse;
import com.intsof.samples.entra.service.EntraIdService;
import com.intsof.samples.entra.service.EntraIdService.EntraTokenValidationResult;
import com.intsof.samples.entra.service.EntraIdService.EntraUserProfile;
import com.intsof.samples.entra.service.VerifiedAppService;
import com.intsof.samples.entra.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntraAuthControllerTest {

    @InjectMocks
    private EntraAuthController controller;

    @Mock
    private EntraIdService entraIdService;

    @Mock
    private VerifiedAppService verifiedAppService;

    @Mock
    private JwtService jwtService;

    @Test
    void loginWithCredentials_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@example.com");
        req.setPassword("pass");

        EntraUserProfile profile = new EntraUserProfile(
                "user@example.com", "Name", "Given", "Family", "sub",
                List.of("USER"), List.of());
        EntraTokenValidationResult result = new EntraTokenValidationResult(true, "Success", profile);
        when(entraIdService.authenticateWithCredentials("user@example.com", "pass")).thenReturn(result);
        when(verifiedAppService.getVerifiedApplicationsForDomain("example.com")).thenReturn(List.of("app1", "app2"));
        when(jwtService.generateToken(eq("user@example.com"), eq(profile.getRoles()), anyMap())).thenReturn("access-token");
        when(jwtService.generateRefreshToken("user@example.com")).thenReturn("refresh-token");

        ResponseEntity<?> response = controller.loginWithCredentials(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TokenResponse);
        TokenResponse tokenResponse = (TokenResponse) response.getBody();
        assertEquals("access-token", tokenResponse.getAccessToken());
        assertEquals("refresh-token", tokenResponse.getRefreshToken());
        assertEquals(List.of("app1", "app2"), tokenResponse.getApplications());
    }

    @Test
    void loginWithCredentials_failure() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@example.com");
        req.setPassword("bad");

        EntraTokenValidationResult result = new EntraTokenValidationResult(false, "Invalid credentials", null);
        when(entraIdService.authenticateWithCredentials("user@example.com", "bad")).thenReturn(result);

        ResponseEntity<?> response = controller.loginWithCredentials(req);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertEquals("Invalid credentials", error.get("error"));
    }
}
