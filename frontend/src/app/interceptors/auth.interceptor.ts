import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router, private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    let authReq = req;
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Try to refresh token and retry request
          const refreshToken = localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken');
          if (refreshToken && !req.url.includes('/refresh')) {
            // Attempt token refresh with Phase 6 token rotation
            return this.authService.refreshToken().pipe(
              switchMap(() => {
                // Retry the original request with new token
                const newToken = localStorage.getItem('accessToken');
                if (newToken) {
                  const retryReq = req.clone({
                    setHeaders: {
                      Authorization: `Bearer ${newToken}`
                    }
                  });
                  return next.handle(retryReq);
                } else {
                  this.router.navigate(['']);
                  return throwError(() => error);
                }
              }),
              catchError((refreshError) => {
                // Refresh failed - redirect to login
                this.router.navigate(['']);
                return throwError(() => refreshError);
              })
            );
          } else {
            // No refresh token or refresh endpoint - redirect to login
            this.router.navigate(['']);
          }
        } else if (error.status === 429) {
          // Rate limit exceeded - Phase 6 feature
          console.warn('Rate limit exceeded. Please try again later.');
          // Could show a user-friendly message here
        }
        return throwError(() => error);
      })
    );
  }
}
