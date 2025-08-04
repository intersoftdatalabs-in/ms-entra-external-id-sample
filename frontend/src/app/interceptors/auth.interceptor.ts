import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

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
          if (refreshToken) {
            // Ideally, inject AuthService and call refreshToken()
            // For demo, just redirect to login
            this.router.navigate(['']);
          } else {
            this.router.navigate(['']);
          }
        }
        return throwError(() => error);
      })
    );
  }
}
