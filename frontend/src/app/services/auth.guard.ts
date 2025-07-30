import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    const authenticated = sessionStorage.getItem('authenticated') === 'true';
    if (!authenticated) {
      this.router.navigate(['']);
      return false;
    }
    return true;
  }
}
