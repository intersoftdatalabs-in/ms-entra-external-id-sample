# JWT Token Support Implementation Plan

## Overview
This plan will add JWT (JSON Web Token) support to the existing MS Entra External ID authentication system, enabling stateless authentication with token-based security for both database and SSO authentication providers.

## Current State Analysis
- ✅ **nimbus-jose-jwt** dependency already included in pom.xml
- ✅ Spring Boot 3.x with Spring Security crypto
- ✅ Dual authentication providers (Database + MS Entra External ID SSO)
- ✅ Filter-based authentication architecture
- ❌ No JWT token generation/validation
- ❌ Frontend stores session-based authentication only
- ❌ No token refresh mechanism

## Implementation Plan

### Phase 1: Backend JWT Infrastructure

#### 1.1 JWT Service Implementation
**Create**: `backend/src/main/java/com/intsof/samples/entra/service/JwtService.java`

**Features:**
- Token generation with configurable expiration
- Token validation and parsing
- Refresh token support
- User claims extraction
- Role-based claims (for future authorization)

**Dependencies:** Use existing `nimbus-jose-jwt` library

#### 1.2 JWT Configuration
**Update**: application.properties

**Add properties:**
```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=3600000
jwt.refresh.expiration=86400000
jwt.issuer=ms-entra-external-id-sample
```

#### 1.3 Enhanced Authentication Result
**Update**: AuthenticationResult.java

**Add fields:**
- `String accessToken`
- `String refreshToken`
- `long expiresIn`
- `List<String> roles`

#### 1.4 JWT Token Response DTO
**Create**: `backend/src/main/java/com/intsof/samples/entra/dto/TokenResponse.java`

**Fields:**
- Access token
- Refresh token
- Token type ("Bearer")
- Expires in seconds
- User information

### Phase 2: Authentication Filter Enhancement

#### 2.1 Update AuthenticationFilter
**File**: AuthenticationFilter.java

**Changes:**
1. Add JWT token validation for protected routes
2. Extract token from `Authorization: Bearer <token>` header
3. Generate JWT tokens on successful login
4. Return structured JSON response with tokens
5. Add token refresh endpoint (`/refresh`)

**New endpoints to handle:**
- `POST /login` - Generate JWT tokens
- `POST /refresh` - Refresh access token
- `POST /logout` - Invalidate tokens (optional blacklist)

#### 2.2 Security Provider Updates
**Files**: `DatabaseSecurityProvider.java`, `EntraExternalIdSSOProvider.java`

**Changes:**
- Return enhanced `AuthenticationResult` with user roles
- Add user role extraction logic
- Support for token-based authentication validation

### Phase 3: Frontend JWT Integration

#### 3.1 Auth Service Enhancement
**File**: auth.service.ts

**New features:**
- Store JWT tokens in localStorage/sessionStorage
- Add Authorization header to all HTTP requests
- Implement token refresh logic
- Token expiration handling
- Automatic logout on token expiration

#### 3.2 HTTP Interceptor
**Create**: `frontend/src/app/interceptors/auth.interceptor.ts`

**Features:**
- Automatically add Bearer token to requests
- Handle 401 responses with token refresh
- Redirect to login on authentication failure

#### 3.3 Auth Guard Enhancement
**File**: auth.guard.ts

**Updates:**
- Validate JWT token instead of session storage
- Check token expiration
- Attempt token refresh if expired

### Phase 4: User Role Support

#### 4.1 User Model Enhancement
**Update**: `User.java` entity

**Add:**
- `@ElementCollection` roles field
- Default role assignment logic

#### 4.2 Database Schema Update
**Update**: `data.sql`

**Changes:**
- Add user_roles table
- Populate default roles for existing users

#### 4.3 Role-based Authorization
**Create**: Custom annotations and aspects for method-level security

### Phase 5: MS Entra External ID JWT Integration

#### 5.1 Entra ID Token Validation
**Update**: `EntraExternalIdSSOProvider.java`

**Features:**
- Validate Entra ID tokens
- Extract user claims from Entra tokens
- Map Entra roles to application roles
- Hybrid token approach (validate Entra + issue app tokens)

#### 5.2 MSAL4J Integration Enhancement
**Leverage existing MSAL4J dependency for:**
- Token validation
- User profile extraction
- Role/group claims processing

### Phase 6: Advanced Features

#### 6.1 Token Refresh Strategy
- Sliding window refresh
- Refresh token rotation
- Refresh token family tracking

#### 6.2 Token Blacklisting (Optional)
- Redis/In-memory token blacklist
- Logout token invalidation
- Revoked token tracking

#### 6.3 Security Enhancements
- Token encryption (JWE)
- Rate limiting for auth endpoints
- Audit logging for authentication events

## Implementation Priority

### High Priority (MVP)
1. JWT Service implementation
2. Authentication Filter JWT support
3. Frontend token storage and usage
4. Basic token refresh

### Medium Priority
1. Role-based authorization
2. Enhanced Entra ID integration
3. HTTP Interceptor
4. Token blacklisting

### Low Priority (Future Enhancements)
1. Token encryption
2. Advanced refresh strategies
3. Audit logging
4. Rate limiting

## Security Considerations

1. **Token Storage**: Use secure storage mechanisms
2. **Secret Management**: Use environment variables for JWT secrets
3. **Token Expiration**: Short-lived access tokens (1 hour)
4. **Refresh Tokens**: Longer-lived but rotatable
5. **HTTPS Only**: Ensure all token exchanges over HTTPS
6. **XSS Protection**: Secure token storage against XSS attacks

## Testing Strategy

1. **Unit Tests**: JWT service methods
2. **Integration Tests**: End-to-end authentication flows
3. **Security Tests**: Token validation and expiration
4. **Frontend Tests**: Auth service and interceptor

## Migration Strategy

1. **Backward Compatibility**: Support both session and JWT during transition
2. **Gradual Rollout**: Enable JWT for new users first
3. **Feature Flags**: Toggle between authentication methods
4. **Data Migration**: Convert existing sessions to tokens

This plan provides a comprehensive roadmap for implementing JWT token support while maintaining the existing dual authentication provider architecture and ensuring security best practices.