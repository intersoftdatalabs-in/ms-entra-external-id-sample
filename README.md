# MS Entra External ID Sample Application

## Overview
This project demonstrates a modern full-stack application with:
- **Angular 19.x Frontend**
- **Spring Boot 3.x Backend**
- **MS Entra External ID SSO** and **Database Authentication**
- **JWT-based stateless authentication**
- **Role-based authorization**
- **Advanced security features**: token refresh, blacklisting, rate limiting, audit logging

## Features
- **Dual Authentication**: MS Entra SSO and database login
- **JWT Token Issuance & Validation**: Stateless, secure API access
- **Token Refresh & Rotation**: Sliding window, family tracking
- **Token Blacklisting**: In-memory blacklist for logout/revocation
- **Rate Limiting**: Configurable per-IP and per-user limits
- **Audit Logging**: File-based logs for all authentication events
- **Role-based UI**: Frontend adapts to user roles
- **Frontend/Backend Unit & Integration Tests**

## Prerequisites
- **Java 17** (or newer)
- **Node.js 18+** and **npm**
- **Maven**
- **Angular CLI**

## Environment Variables
Set these environment variables before running the backend. They will be picked up by `application.properties`:

### Linux/macOS
```bash
export SSO_REGISTRATION_AZURE_CLIENT_ID=your-azure-client-id
export SSO_REGISTRATION_AZURE_TENANT_ID=your-azure-tenant-id
export SSO_REGISTRATION_AZURE_CLIENT_SECRET=your-azure-client-secret
export SSO_PROVIDER_AZURE_AUTHORIZATION_URI=https://login.microsoftonline.com/
export SSO_PROVIDER_TOKEN_URI=https://login.microsoftonline.com/oauth2/v2.0/token
export SSO_PROVIDER_USER_INFO_URI=https://graph.microsoft.com/oidc/userinfo
export JWT_SECRET=your-256-bit-secret-key-here
export JWT_ISSUER=ms-entra-external-id-sample
export JWT_EXPIRATION=3600000
export JWT_REFRESH_EXPIRATION=86400000
export AUTH_RATE_LIMIT_MAX_ATTEMPTS=5
export AUTH_RATE_LIMIT_WINDOW_MINUTES=1
export AUTH_RATE_LIMIT_ENABLED=true
export AUDIT_LOG_LEVEL=INFO
export AUDIT_LOG_FILE=logs/audit.log
```

### Windows (Command Prompt)
```cmd
set SSO_REGISTRATION_AZURE_CLIENT_ID=your-azure-client-id
set SSO_REGISTRATION_AZURE_TENANT_ID=your-azure-tenant-id
set SSO_REGISTRATION_AZURE_CLIENT_SECRET=your-azure-client-secret
set SSO_PROVIDER_AZURE_AUTHORIZATION_URI=https://login.microsoftonline.com/
set SSO_PROVIDER_TOKEN_URI=https://login.microsoftonline.com/oauth2/v2.0/token
set SSO_PROVIDER_USER_INFO_URI=https://graph.microsoft.com/oidc/userinfo
set JWT_SECRET=your-256-bit-secret-key-here
set JWT_ISSUER=ms-entra-external-id-sample
set JWT_EXPIRATION=3600000
set JWT_REFRESH_EXPIRATION=86400000
set AUTH_RATE_LIMIT_MAX_ATTEMPTS=5
set AUTH_RATE_LIMIT_WINDOW_MINUTES=1
set AUTH_RATE_LIMIT_ENABLED=true
set AUDIT_LOG_LEVEL=INFO
set AUDIT_LOG_FILE=logs/audit.log
```

### Windows (PowerShell)
```powershell
$env:SSO_REGISTRATION_AZURE_CLIENT_ID="your-azure-client-id"
$env:SSO_REGISTRATION_AZURE_TENANT_ID="your-azure-tenant-id"
$env:SSO_REGISTRATION_AZURE_CLIENT_SECRET="your-azure-client-secret"
$env:SSO_PROVIDER_AZURE_AUTHORIZATION_URI="https://login.microsoftonline.com/"
$env:SSO_PROVIDER_TOKEN_URI="https://login.microsoftonline.com/oauth2/v2.0/token"
$env:SSO_PROVIDER_USER_INFO_URI="https://graph.microsoft.com/oidc/userinfo"
$env:JWT_SECRET="your-256-bit-secret-key-here"
$env:JWT_ISSUER="ms-entra-external-id-sample"
$env:JWT_EXPIRATION="3600000"
$env:JWT_REFRESH_EXPIRATION="86400000"
$env:AUTH_RATE_LIMIT_MAX_ATTEMPTS="5"
$env:AUTH_RATE_LIMIT_WINDOW_MINUTES="1"
$env:AUTH_RATE_LIMIT_ENABLED="true"
$env:AUDIT_LOG_LEVEL="INFO"
$env:AUDIT_LOG_FILE="logs/audit.log"
```

You can also create a `.env` file in the project root with these values for convenience.

## Backend: Build & Run

### Linux/macOS
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Windows (Command Prompt)
```cmd
cd backend
mvn clean install
mvn spring-boot:run
```

### Windows (PowerShell)
```powershell
cd backend
mvn clean install
mvn spring-boot:run
```

### Run Backend Tests
#### Linux/macOS
```bash
mvn test
```
#### Windows (Command Prompt)
```cmd
mvn test
```
#### Windows (PowerShell)
```powershell
mvn test
```

## Frontend: Build & Run

### Linux/macOS
```bash
cd frontend
npm install
ng serve
```

### Windows (Command Prompt)
```cmd
cd frontend
npm install
ng serve
```

### Windows (PowerShell)
```powershell
cd frontend
npm install
ng serve
```

### Run Frontend Tests
#### Linux/macOS
```bash
ng test
```
#### Windows (Command Prompt)
```cmd
ng test
```
#### Windows (PowerShell)
```powershell
ng test
```

## Usage
- Access the frontend at [http://localhost:4200](http://localhost:4200)
- Backend runs at [http://localhost:8080](http://localhost:8080)
- Login with either MS Entra SSO or database credentials
- JWT tokens are stored in browser storage and sent with API requests

## Configuration
- All backend config is in `backend/src/main/resources/application.properties` and can be overridden by environment variables
- Frontend config is in `frontend/src/environments/`

## Security Notes
- Use strong secrets for JWT and Azure credentials
- Always run behind HTTPS in production
- Audit logs are written to `logs/audit.log` by default
- Rate limiting and blacklisting are enabled by default

## Consuming the Security Module

The authentication and SSO logic has been extracted into a standalone library (`security-module`), which you can incorporate into other Spring Boot services.

Add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.intsof.samples.security</groupId>
  <artifactId>security-module</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

Implement the required SPIs in your application:

```java
import com.intsof.samples.security.spi.UserAuthenticationService;
import com.intsof.samples.security.spi.ExternalIdTokenService;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationServiceImpl implements UserAuthenticationService {
    @Override
    public boolean authenticate(String email, String password) {
        // Your authentication logic (e.g., database lookup)
        return yourUserService.authenticate(email, password);
    }
}

@Component
public class ExternalIdTokenServiceImpl implements ExternalIdTokenService {
    @Override
    public CompletableFuture<IAuthenticationResult> acquireTokenByAuthorizationCode(String code, String redirectUri, Set<String> scopes) {
        // Your token acquisition logic
        return yourEntraIdService.acquireTokenByAuthorizationCode(code, redirectUri, scopes);
    }

    @Override
    public ExternalUserProfile getUserProfile(String accessToken) {
        // Your profile extraction logic
        return yourEntraIdService.getUserProfile(accessToken);
    }

    @Override
    public ExternalTokenValidationResult validateToken(String token) {
        // Your token validation logic
        return yourEntraIdService.validateToken(token);
    }
}
```

Configure the `SecurityManager` bean:

```java
import com.intsof.samples.security.SecurityManager;
import com.intsof.samples.security.DatabaseSecurityProvider;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityModuleConfig {
    @Bean
    public SecurityManager securityManager(DatabaseSecurityProvider dbProvider, EntraExternalIdSSOProvider ssoProvider) {
        SecurityManager manager = new SecurityManager(dbProvider);
        manager.registerProvider("your-sso-domain.com", ssoProvider);
        return manager;
    }
}
```

Now you can autowire `SecurityManager` anywhere for authentication.

## Support
For issues or questions, please open an issue in this repository.
