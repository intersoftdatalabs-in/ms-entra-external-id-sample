# SSO Authentication Flow Diagram

## Overview
This diagram shows the complete SSO authentication flow for the MS Entra External ID sample application.

```mermaid
sequenceDiagram
    participant U as User/Frontend
    participant AF as AuthenticationFilter
    participant AC as AuthCheckController
    participant SM as SecurityManager
    participant SSO as EntraExternalIdSSOProvider
    participant EC as EntraAuthController
    participant MS as Microsoft Entra
    participant JWT as JwtService

    Note over U,JWT: Phase 1: Authentication Method Detection
    
    U->>+AF: POST /auth/check-method?email=user@gmail.com
    Note over AF: Checks if path is whitelisted
    AF->>AF: isWhitelistedPath("/auth/check-method") = true
    AF->>+AC: Forward request (no authentication required)
    
    AC->>+SM: Create SecurityManager with providers
    AC->>SM: registerProvider("gmail.com", ssoProvider)
    AC->>SM: requiresSSO("user@gmail.com")
    SM->>SM: extractDomain("user@gmail.com") = "gmail.com"
    SM->>SM: getSecurityProvider("gmail.com") = EntraExternalIdSSOProvider
    SM-->>AC: returns true (SSO required)
    
    AC-->>-AF: Response: {"requiresSSO": true, "authMethod": "SSO", "authorizationUrl": "/auth/entra/authorization-url"}
    AF-->>-U: 200 OK with SSO redirect info

    Note over U,JWT: Phase 2: SSO Authorization URL Generation
    
    U->>+AF: GET /auth/entra/authorization-url?redirect_uri=http://localhost:4200/callback
    AF->>AF: isWhitelistedPath("/auth/entra/authorization-url") = true
    AF->>+EC: Forward request (no authentication required)
    
    EC->>EC: buildAuthorizationUrl(redirectUri, state)
    Note over EC: Builds: https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize?client_id={id}&response_type=code&redirect_uri={uri}&scope=openid%20profile%20email
    EC-->>-AF: Response: {"authorization_url": "https://login.microsoftonline.com/..."}
    AF-->>-U: 200 OK with authorization URL

    Note over U,JWT: Phase 3: OAuth Authorization Flow
    
    U->>+MS: Redirect to authorization URL
    Note over MS: User authenticates with Microsoft
    MS-->>-U: Redirect to callback with authorization code
    
    Note over U,JWT: Phase 4: Authorization Code Exchange
    
    U->>+AF: POST /auth/entra/callback?code={auth_code}&redirect_uri={uri}
    AF->>AF: isWhitelistedPath("/auth/entra/callback") = true
    AF->>+EC: Forward request (no authentication required)
    
    EC->>+SSO: authenticateWithAuthorizationCode(code, redirectUri)
    SSO->>+MS: Exchange authorization code for tokens
    MS-->>-SSO: Access token + ID token + user info
    SSO->>SSO: Extract user profile and roles
    SSO-->>-EC: AuthenticationResult(success=true, userId, roles)
    
    EC->>+JWT: generateToken(userId, roles, null)
    JWT-->>-EC: Application JWT access token
    EC->>+JWT: generateRefreshToken(userId)
    JWT-->>-EC: Application JWT refresh token
    
    EC-->>-AF: TokenResponse with JWT tokens
    AF-->>-U: 200 OK with application tokens

    Note over U,JWT: Phase 5: Authenticated API Access
    
    U->>+AF: GET /some-protected-endpoint (Authorization: Bearer {jwt})
    AF->>AF: isWhitelistedPath("/some-protected-endpoint") = false
    AF->>+JWT: validateTokenWithBlacklist(jwt)
    JWT-->>-AF: Token valid
    AF->>+AF: Chain to next filter/controller
    AF-->>-U: Protected resource response

    Note over U,JWT: Alternative Flow: Direct Login Attempt (SSO Domain)
    
    U->>+AF: POST /login (X-Email: user@gmail.com, X-Password: password)
    AF->>AF: isWhitelistedPath("/login") = true
    AF->>AF: Handle login request
    AF->>+SM: authenticate("user@gmail.com", "password")
    SM->>SM: getSecurityProvider("user@gmail.com") = EntraExternalIdSSOProvider
    SM->>SM: Provider instanceof EntraExternalIdSSOProvider = true
    SM-->>-AF: AuthenticationResult(success=false, message="SSO_REDIRECT_REQUIRED")
    AF-->>-U: 401 with SSO redirect info

    Note over U,JWT: Alternative Flow: Database Authentication (Non-SSO Domain)
    
    U->>+AF: POST /login (X-Email: user@example.com, X-Password: password)
    AF->>AF: isWhitelistedPath("/login") = true
    AF->>AF: Handle login request
    AF->>+SM: authenticate("user@example.com", "password")
    SM->>SM: getSecurityProvider("user@example.com") = DatabaseSecurityProvider
    SM->>SM: provider.authenticate(email, password)
    SM-->>-AF: AuthenticationResult(success=true/false, userId, roles)
    AF->>+JWT: generateToken(userId, roles, null) [if successful]
    JWT-->>-AF: JWT tokens [if successful]
    AF-->>-U: 200 OK with tokens OR 401 with error
```

## Key Components

### 1. AuthenticationFilter
- **Purpose**: Central filter that handles all authentication and authorization
- **Whitelist**: Allows unauthenticated access to authentication endpoints
- **JWT Validation**: Validates tokens for protected endpoints
- **SSO Detection**: Detects when SSO redirect is required

### 2. AuthCheckController
- **Endpoint**: `POST /auth/check-method`
- **Purpose**: Determines authentication method (SSO vs Password) for email domain
- **Response**: Returns `requiresSSO` flag and authorization URL if needed

### 3. EntraAuthController
- **Endpoints**: 
  - `GET /auth/entra/authorization-url` - Generates OAuth authorization URL
  - `POST /auth/entra/callback` - Handles OAuth callback with authorization code
  - `POST /auth/entra/validate` - Validates existing Entra tokens
- **Purpose**: Manages complete OAuth 2.0 / OpenID Connect flow

### 4. SecurityManager
- **Purpose**: Routes authentication requests to appropriate providers
- **Providers**: 
  - `DatabaseSecurityProvider` for password-based auth
  - `EntraExternalIdSSOProvider` for SSO domains
- **Domain Mapping**: Maps email domains to authentication providers

### 5. EntraExternalIdSSOProvider
- **Purpose**: Handles Microsoft Entra External ID authentication
- **Methods**:
  - `authenticateWithAuthorizationCode()` - Exchanges auth code for tokens
  - `validateEntraToken()` - Validates existing Entra tokens
  - `authenticate()` - Always returns failure (SSO redirect required)

## Configuration

### SSO Enabled Domains
```properties
sso.enabled-domains=gmail.com,intsof.com,microsoft.com
```

### Entra Configuration
```properties
sso.registration.azure.client-id=${SSO_REGISTRATION_AZURE_CLIENT_ID}
sso.registration.azure.tenant-id=${SSO_REGISTRATION_AZURE_TENANT_ID}
sso.registration.azure.client-secret=${SSO_REGISTRATION_AZURE_CLIENT_SECRET}
sso.registration.azure.redirect-uri=http://localhost:4200/auth/callback
sso.provider.azure.authorization-uri=https://login.microsoftonline.com/
```

### Whitelisted Endpoints (No Authentication Required)
- `/login` - Password authentication endpoint
- `/refresh` - Token refresh endpoint  
- `/logout` - Logout endpoint
- `/auth/check-method` - Authentication method detection
- `/auth/entra/authorization-url` - OAuth URL generation
- `/auth/entra/callback` - OAuth callback handler
- `/auth/entra/validate` - Token validation

## Security Features

1. **Rate Limiting**: Prevents brute force attacks on login endpoints
2. **Token Blacklisting**: Invalidates compromised tokens
3. **Token Rotation**: Generates new refresh tokens on each use
4. **Audit Logging**: Tracks all authentication events
5. **JWT Validation**: Validates tokens for protected endpoints
6. **Domain-based Routing**: Automatically routes to appropriate auth provider

## Error Handling

1. **SSO Redirect Required**: Returns 401 with redirect information
2. **Invalid Credentials**: Returns 401 with error message
3. **Missing Email**: Returns 400 with validation error
4. **Token Validation Failure**: Returns 401 with token error
5. **OAuth Errors**: Returns appropriate HTTP status with error details
