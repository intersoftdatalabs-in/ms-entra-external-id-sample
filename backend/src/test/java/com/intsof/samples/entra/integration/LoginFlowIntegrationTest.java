package com.intsof.samples.entra.integration;

import com.intsof.samples.entra.service.EntraIdService;
import com.intsof.samples.entra.service.EntraIdService.EntraTokenValidationResult;
import com.intsof.samples.entra.service.EntraIdService.EntraUserProfile;
import com.intsof.samples.entra.service.JwtService;
import com.intsof.samples.entra.service.VerifiedAppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.sql.init.mode=never"})
@AutoConfigureMockMvc(addFilters = false)
class LoginFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntraIdService entraIdService;

    @MockBean
    private VerifiedAppService verifiedAppService;

    @MockBean
    private JwtService jwtService;

    @Test
    void whenValidCredentials_thenReturnsJwtAndApplications() throws Exception {
        String username = "user@example.com";
        String password = "pass";
        List<String> roles = List.of("USER");
        EntraUserProfile profile = new EntraUserProfile(
                username, "Name", "Given", "Family", "sub", roles, List.of());
        EntraTokenValidationResult validationResult = new EntraTokenValidationResult(true, "Success", profile);
        when(entraIdService.authenticateWithCredentials(eq(username), eq(password))).thenReturn(validationResult);
        when(verifiedAppService.getVerifiedApplicationsForDomain("example.com")).thenReturn(List.of("app1", "app2"));
        when(jwtService.generateToken(eq(username), eq(roles), anyMap())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(eq(username))).thenReturn("refresh-token");

        String requestBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/auth/entra/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.applications[0]").value("app1"))
            .andExpect(jsonPath("$.applications[1]").value("app2"));
    }

    @Test
    void whenInvalidCredentials_thenReturnsUnauthorized() throws Exception {
        String username = "user@example.com";
        String password = "bad";
        EntraTokenValidationResult validationResult = new EntraTokenValidationResult(false, "Invalid credentials", null);
        when(entraIdService.authenticateWithCredentials(eq(username), eq(password))).thenReturn(validationResult);

        String requestBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        mockMvc.perform(post("/auth/entra/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }
}
