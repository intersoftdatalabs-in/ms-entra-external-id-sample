package com.intsof.samples.entra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Binds all SSO-related settings from application.properties/environment variables so that other
 * beans (controllers, services, etc.) can easily consume them without sprinkling @Value all over
 * the codebase.
 */
@Component
@ConfigurationProperties(prefix = "sso")
public class SsoConfigProperties {

    /** Comma-separated list of email domains that must authenticate through SSO. */
    private List<String> enabledDomains;

    private Registration registration = new Registration();
    private Provider provider = new Provider();
    private Map<String, List<String>> verifiedApps = new HashMap<>();

    // ---------------------------------------------------------------------
    // Getters / setters
    // ---------------------------------------------------------------------

    public List<String> getEnabledDomains() {
        return enabledDomains;
    }

    public void setEnabledDomains(List<String> enabledDomains) {
        this.enabledDomains = enabledDomains;
    }

    public Registration getRegistration() {
        return registration;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Map<String, List<String>> getVerifiedApps() {
        return verifiedApps;
    }

    public void setVerifiedApps(Map<String, List<String>> verifiedApps) {
        this.verifiedApps = verifiedApps;
    }

    // =====================================================================
    // Nested property classes mirroring spring.security.oauth2.* style:
    // sso.registration.azure.* and sso.provider.*
    // =====================================================================

    public static class Registration {
        private Azure azure = new Azure();

        public Azure getAzure() {
            return azure;
        }

        public void setAzure(Azure azure) {
            this.azure = azure;
        }

        public static class Azure {
            private String clientId;
            private String tenantId;
            private String clientSecret;
            private String authorizationGrantType;
            private String redirectUri;
            private String scope; // space- or comma-separated

            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getTenantId() {
                return tenantId;
            }

            public void setTenantId(String tenantId) {
                this.tenantId = tenantId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }

            public String getAuthorizationGrantType() {
                return authorizationGrantType;
            }

            public void setAuthorizationGrantType(String authorizationGrantType) {
                this.authorizationGrantType = authorizationGrantType;
            }

            public String getRedirectUri() {
                return redirectUri;
            }

            public void setRedirectUri(String redirectUri) {
                this.redirectUri = redirectUri;
            }

            public String getScope() {
                return scope;
            }

            public void setScope(String scope) {
                this.scope = scope;
            }
        }
    }

    public static class Provider {
        private Azure azure = new Azure();
        private String tokenUri;
        private String userInfoUri;
        private String userNameAttribute;

        public Azure getAzure() {
            return azure;
        }

        public void setAzure(Azure azure) {
            this.azure = azure;
        }

        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }

        public String getUserInfoUri() {
            return userInfoUri;
        }

        public void setUserInfoUri(String userInfoUri) {
            this.userInfoUri = userInfoUri;
        }

        public String getUserNameAttribute() {
            return userNameAttribute;
        }

        public void setUserNameAttribute(String userNameAttribute) {
            this.userNameAttribute = userNameAttribute;
        }

        public static class Azure {
            private String authorizationUri;
            private String authorizePath = "oauth2/v2.0/authorize";

            public String getAuthorizationUri() {
                return authorizationUri;
            }
            public String getAuthorizePath() {
                return authorizePath;
            }

            public void setAuthorizationUri(String authorizationUri) {
                this.authorizationUri = authorizationUri;
            }
            public void setAuthorizePath(String authorizePath) {
                this.authorizePath = authorizePath;
            }
        }
    }
}

