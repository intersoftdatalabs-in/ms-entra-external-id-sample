import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="callback-container">
      <div class="loading-message">
        <h2>Processing your login...</h2>
        <p>Please wait while we complete your authentication.</p>
        <div *ngIf="errorMessage" class="error-message">
          <h3>Authentication Error</h3>
          <p>{{ errorMessage }}</p>
          <button (click)="goToLogin()">Return to Login</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .callback-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      background-color: #f5f5f5;
    }
    .loading-message {
      text-align: center;
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    .error-message {
      margin-top: 1rem;
      color: #d32f2f;
    }
    button {
      margin-top: 1rem;
      padding: 0.5rem 1rem;
      background-color: #1976d2;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    button:hover {
      background-color: #1565c0;
    }
  `]
})
export class AuthCallbackComponent implements OnInit {
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const state = params['state'];
      const error = params['error'];

      if (error) {
        this.errorMessage = `Authentication failed: ${error}`;
        return;
      }

      if (!code) {
        this.errorMessage = 'No authorization code received';
        return;
      }

      // Validate state parameter
      const storedState = sessionStorage.getItem('sso_state');
      if (state !== storedState) {
        this.errorMessage = 'Invalid state parameter - possible CSRF attack';
        return;
      }

      // Exchange code for tokens
      this.exchangeCodeForTokens(code);
    });
  }

  private exchangeCodeForTokens(code: string) {
    const redirectUri = `${window.location.origin}/auth/callback`;
    
    this.http.post(`${environment.refreshUrl}`, null, {
      params: {
        code: code,
        redirect_uri: redirectUri
      }
    }).subscribe({
      next: (response: any) => {
        if (response.accessToken) {
          // Store tokens
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
          sessionStorage.setItem('authenticated', 'true');
          
          const email = sessionStorage.getItem('sso_email');
          if (email) {
            sessionStorage.setItem('email', email);
          }

          // Clear SSO session data
          sessionStorage.removeItem('sso_state');
          sessionStorage.removeItem('sso_email');

          // Redirect to welcome page
          this.router.navigate(['/welcome'], { state: { email: email } });
        } else {
          this.errorMessage = 'Failed to receive authentication tokens';
        }
      },
      error: (err) => {
        console.error('Token exchange failed:', err);
        this.errorMessage = 'Failed to complete authentication';
      }
    });
  }

  goToLogin() {
    // Clear any stored SSO data
    sessionStorage.removeItem('sso_state');
    sessionStorage.removeItem('sso_email');
    this.router.navigate(['/']);
  }
}
