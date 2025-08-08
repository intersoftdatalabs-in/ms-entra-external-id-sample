package com.intsof.samples.entra.dto;

import java.util.List;

/**
 * A simple data-transfer object returned by /api/sso/config.
 */
public class SsoConfigDto {

    private List<String> enabledDomains;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String redirectUri;
    private List<String> scopes;
    private String clientId;

    public SsoConfigDto() {
    }

    public SsoConfigDto(List<String> enabledDomains,
                        String authorizationEndpoint,
                        String tokenEndpoint,
                        String redirectUri,
                        List<String> scopes,
                        String clientId) {
        this.enabledDomains = enabledDomains;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
        this.clientId = clientId;
    }

    public List<String> getEnabledDomains() {
        return enabledDomains;
    }

    public void setEnabledDomains(List<String> enabledDomains) {
        this.enabledDomains = enabledDomains;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

