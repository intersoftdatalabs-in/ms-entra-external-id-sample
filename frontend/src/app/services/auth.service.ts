
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { throwError, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private loginUrl = 'http://localhost:8080/login';

  constructor(private http: HttpClient) {}

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
