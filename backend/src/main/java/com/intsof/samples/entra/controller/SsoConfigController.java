package com.intsof.samples.entra.controller;

import com.intsof.samples.entra.config.SsoConfigProperties;
import com.intsof.samples.entra.dto.SsoConfigDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoint that exposes a subset of SSO configuration properties so that browser
 * clients can dynamically discover how to drive the Microsoft Entra External ID flow
 * without hard-coded environment variables.
 */
@RestController
@RequestMapping("/api/sso")
public class SsoConfigController {

    private final SsoConfigProperties props;

    @Autowired
    public SsoConfigController(SsoConfigProperties props) {
        this.props = props;
    }

    @GetMapping("/config")
    public ResponseEntity<SsoConfigDto> getConfig() {
        // Build authorization endpoint: <authorizationUri>/<tenantId>/oauth2/v2.0/authorize
        String baseAuthUri = props.getProvider().getAzure().getAuthorizationUri();
        if (!StringUtils.hasText(baseAuthUri)) {
            baseAuthUri = "";
        }
        if (!baseAuthUri.endsWith("/")) {
            baseAuthUri += "/";
        }
        String tenantId = props.getRegistration().getAzure().getTenantId();
        String authorizePath = props.getProvider().getAzure().getAuthorizePath();
        if (!StringUtils.hasText(authorizePath)) {
            authorizePath = "oauth2/v2.0/authorize";
        }
        //String authorizationEndpoint = baseAuthUri + (tenantId != null ? tenantId + "/" : "") + authorizePath;
        String authorizationEndpoint = baseAuthUri;

        String tokenEndpoint = props.getProvider().getTokenUri();
        String redirectUri = props.getRegistration().getAzure().getRedirectUri();

        // Split scopes on whitespace or comma and trim blanks
        String scopesRaw = props.getRegistration().getAzure().getScope();
        List<String> scopes;
        if (!StringUtils.hasText(scopesRaw)) {
            scopes = List.of();
        } else {
            scopes = Arrays.stream(scopesRaw.split("[\\s,]+"))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
        }

        String clientId = props.getRegistration().getAzure().getClientId();

        SsoConfigDto dto = new SsoConfigDto(
                props.getEnabledDomains(),
                authorizationEndpoint,
                tokenEndpoint,
                redirectUri,
                scopes,
                clientId
        );
        return ResponseEntity.ok(dto);
    }
}

