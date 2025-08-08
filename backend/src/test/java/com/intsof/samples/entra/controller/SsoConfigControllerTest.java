package com.intsof.samples.entra.controller;

import com.intsof.samples.entra.config.SsoConfigProperties;
import com.intsof.samples.entra.dto.SsoConfigDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SsoConfigController}
 */
public class SsoConfigControllerTest {

    private SsoConfigController controller;

    @BeforeEach
    void setUp() {
        // Prepare a mock SsoConfigProperties with predictable values
        SsoConfigProperties mockProps = Mockito.mock(SsoConfigProperties.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(mockProps.getEnabledDomains()).thenReturn(List.of("example.com"));
        Mockito.when(mockProps.getProvider().getAzure().getAuthorizationUri()).thenReturn("https://login.microsoftonline.com/");
        Mockito.when(mockProps.getRegistration().getAzure().getTenantId()).thenReturn("demo-tenant");
        Mockito.when(mockProps.getProvider().getTokenUri()).thenReturn("https://login.microsoftonline.com/oauth2/v2.0/token");
        Mockito.when(mockProps.getRegistration().getAzure().getRedirectUri()).thenReturn("http://localhost:8080/auth/entra/callback");
        Mockito.when(mockProps.getRegistration().getAzure().getScope()).thenReturn("openid profile email");
        Mockito.when(mockProps.getRegistration().getAzure().getClientId()).thenReturn("demo-client-id");

        controller = new SsoConfigController(mockProps);
    }

    @Test
    void testGetConfig() {
        ResponseEntity<SsoConfigDto> response = controller.getConfig();
        assertEquals(200, response.getStatusCode().value());

        SsoConfigDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(List.of("example.com"), dto.getEnabledDomains());
        assertEquals("https://login.microsoftonline.com/demo-tenant/oauth2/v2.0/authorize", dto.getAuthorizationEndpoint());
        assertEquals("https://login.microsoftonline.com/oauth2/v2.0/token", dto.getTokenEndpoint());
        assertEquals("http://localhost:8080/auth/entra/callback", dto.getRedirectUri());
        assertEquals(List.of("openid", "profile", "email"), dto.getScopes());
        assertEquals("demo-client-id", dto.getClientId());
    }
}

