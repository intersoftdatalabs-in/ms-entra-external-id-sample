import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  email: string = '';
  password: string = '';
  errorMessage: string = '';
  ssoConfig: any | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.ssoConfig = this.authService.getSsoConfig();
  }

  login() {
    // First check if this email requires SSO
    this.authService.checkAuthMethod(this.email).subscribe({
      next: (authCheckResponse) => {
        if (authCheckResponse.requiresSSO) {
          // Email domain requires SSO - redirect to SSO flow
          this.authService.initiateSSO(this.email);
        } else {
          // Use password authentication
          this.performPasswordLogin();
        }
      },
      error: (err) => {
        console.error('Failed to check auth method:', err);
        // Fallback to password authentication
        this.performPasswordLogin();
      }
    });
  }

  private performPasswordLogin() {
    this.authService.login(this.email, this.password).subscribe({
      next: response => {
        if (response.error) {
          // Check if it's SSO redirect required
          if (response.error === 'SSO_REDIRECT_REQUIRED') {
            this.authService.initiateSSO(this.email);
            return;
          }
          this.errorMessage = response.error;
        } else if (this.authService.isAuthenticated()) {
          this.errorMessage = '';
          this.router.navigate(['/welcome'], { state: { email: this.email } });
        } else {
          this.errorMessage = 'Login failed.';
        }
      },
      error: (err: HttpErrorResponse) => {
        if (err.error && err.error.error) {
          // Check if it's SSO redirect required
          if (err.error.error === 'SSO_REDIRECT_REQUIRED') {
            this.authService.initiateSSO(this.email);
            return;
          }
          this.errorMessage = err.error.error;
        } else if (err.status === 0) {
          this.errorMessage = 'Cannot connect to server.';
        } else {
          this.errorMessage = 'An unexpected error occurred.';
        }
      }
    });
  }
}
