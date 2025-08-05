import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private authService: AuthService) {}

  canActivate(): boolean {
    if (!this.authService.isAuthenticated()) {
      // Try to refresh token if expired
      const refreshToken = this.authService.getRefreshToken();
      if (refreshToken) {
        this.authService.refreshToken().subscribe({
          next: () => {},
          error: () => {
            this.router.navigate(['']);
          }
        });
        // Allow navigation after refresh attempt
        return true;
      } else {
        this.router.navigate(['']);
        return false;
      }
    }
    return true;
  }
}
