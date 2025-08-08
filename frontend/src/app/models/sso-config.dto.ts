// Represents the configuration settings required for initiating an external SSO flow.
// This mirrors the SsoConfigDto returned by the backend.
export interface SsoConfigDto {
  /**
   * The list of email domains for which SSO should be used.
   */
  enabledDomains: string[];

  /**
   * The authorization endpoint that the frontend should redirect the user to.
   */
  authorizationEndpoint: string;

  /**
   * The token endpoint used during the OAuth2 code-exchange flow.
   */
  tokenEndpoint: string;

  /**
   * The redirect URI registered for the application.
   */
  redirectUri: string;

  /**
   * OAuth2 scopes to request during authorization.
   */
  scopes: string[];

  /**
   * The public client identifier.
   */
  clientId: string;
}

