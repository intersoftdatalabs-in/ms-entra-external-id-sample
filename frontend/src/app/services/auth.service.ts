
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, tap, take } from 'rxjs/operators';
import { throwError, Observable } from 'rxjs';
import { SsoConfigDto } from '../models/sso-config.dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private loginUrl = 'http://localhost:8080/login';
  private authCheckUrl = 'http://localhost:8080/auth/check-method';
  private entraAuthUrl = 'http://localhost:8080/auth/entra/authorization-url';

  // Holds the SSO configuration once retrieved from the backend
  private ssoConfig: SsoConfigDto | null = null;

  constructor(private http: HttpClient) {
    // Eagerly fetch SSO configuration so that UI components can react dynamically
    this.loadSsoConfig();
  }

  /**
   * Retrieve SSO configuration from the backend.
   */
  private loadSsoConfig(): void {
    this.http.get<SsoConfigDto>(`${environment.apiBaseUrl}/api/sso/config`).subscribe({
      next: (config: SsoConfigDto) => {
        this.ssoConfig = config;
      },
      error: err => {
        console.error('Failed to load SSO configuration', err);
        this.ssoConfig = null; // fallback to legacy mode
      }
    });
  }

  /**
   * Expose the last retrieved SSO configuration to callers.
   */
  getSsoConfig(): SsoConfigDto | null {
    return this.ssoConfig;
  }

  login(email: string, password: string): Observable<any> {
    const headers = {
      'X-Email': email,
      'X-Password': password
    };
    return this.http.post(this.loginUrl, {}, { headers }).pipe(
      tap((response: any) => {
        if (response.accessToken) {
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
          sessionStorage.setItem('authenticated', 'true');
          sessionStorage.setItem('email', email);
        }
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Check if user's email domain requires SSO authentication
   */
  checkAuthMethod(email: string): Observable<any> {
    return this.http.post(this.authCheckUrl, null, {
      params: { email: email }
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get authorization URL for SSO authentication
   */
  getAuthorizationUrl(redirectUri: string, state?: string): Observable<any> {
    let params: any = { redirect_uri: redirectUri };
    if (state) {
      params.state = state;
    }
    
    return this.http.get(this.entraAuthUrl, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle SSO redirect - redirect user to Microsoft login
   */
  initiateSSO(email: string): void {
    // Dynamically load SSO configuration from the backend
    this.http.get<SsoConfigDto>(`${environment.apiBaseUrl}/api/sso/config`).pipe(take(1)).subscribe({
      next: (config: SsoConfigDto) => {
        const state = Math.random().toString(36).substring(2, 15);
        const params = new URLSearchParams({
          response_type: 'code',
          client_id: config.clientId || '',
          redirect_uri: config.redirectUri,
          scope: (config.scopes || []).join(' '),
          state,
          login_hint: email
        });
        sessionStorage.setItem('sso_state', state);
        sessionStorage.setItem('sso_email', email);
        window.location.href = `${config.authorizationEndpoint}?${params.toString()}`;
      },
      error: err => {
        console.error('Failed to load SSO configuration:', err);
        // Fallback: legacy flow
        const redirectUri = `${environment.apiBaseUrl}/auth/entra/callback`;
        const state = Math.random().toString(36).substring(2, 15);
        this.getAuthorizationUrl(redirectUri, state).pipe(take(1)).subscribe({
          next: (response: any) => {
            if (response.authorization_url) {
              sessionStorage.setItem('sso_state', state);
              sessionStorage.setItem('sso_email', email);
              window.location.href = response.authorization_url;
            }
          },
          error: err2 => {
            console.error('Failed to get authorization URL:', err2);
            alert('Failed to initiate SSO login');
          }
        });
      }
    });
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken');
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    return !!token && !this.isTokenExpired(token);
  }

  isTokenExpired(token: string): boolean {
    if (!token) return true;
    const payload = this.decodeToken(token);
    if (!payload || !payload.exp) return true;
    const now = Math.floor(Date.now() / 1000);
    return payload.exp < now;
  }

  decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (e) {
      return null;
    }
  }

  getUserRoles(): string[] {
    if (!this.isAuthenticated()) {
      return [];
    }
    const token = this.getAccessToken();
    const payload = this.decodeToken(token || '');
    return payload && payload.roles ? payload.roles : [];
  }

  refreshToken(): Observable<any> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return throwError(() => new Error('No refresh token available'));
    return this.http.post('http://localhost:8080/refresh', {}, {
      headers: { 'X-Refresh-Token': refreshToken }
    }).pipe(
      tap((response: any) => {
        if (response.accessToken) {
          localStorage.setItem('accessToken', response.accessToken);
          // Update refresh token if rotation is enabled
          if (response.refreshToken) {
            localStorage.setItem('refreshToken', response.refreshToken);
          }
        }
      }),
      catchError((error: HttpErrorResponse) => {
        // Handle specific error cases for Phase 6 features
        if (error.status === 401) {
          // Token might be compromised or invalid - clear all tokens
          this.clearTokens();
        }
        return this.handleError(error);
      })
    );
  }

  /**
   * Enhanced logout with token invalidation
   */
  logout(): Observable<any> {
    const token = this.getAccessToken();
    const headers: any = {};
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return this.http.post('http://localhost:8080/logout', {}, { headers })
      .pipe(
        tap(() => {
          this.clearTokens();
        }),
        catchError((error: HttpErrorResponse) => {
          // Clear tokens even if logout fails
          this.clearTokens();
          return this.handleError(error);
        })
      );
  }

  /**
   * Clear all stored tokens
   */
  private clearTokens(): void {
    sessionStorage.clear();
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Client-side error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Server returned code ${error.status}, body was: ${error.error}`;
    }
    return throwError(() => new Error(errorMessage));
  }
}
