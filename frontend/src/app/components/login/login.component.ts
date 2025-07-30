import { Component } from '@angular/core';
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
export class LoginComponent {
  email: string = '';
  password: string = '';
  errorMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.authService.login(this.email, this.password).subscribe({
      next: response => {
        if (response.error) {
          this.errorMessage = response.error;
        } else {
          this.errorMessage = '';
          this.router.navigate(['/welcome'], { state: { email: response.email } });
        }
      },
      error: (err: HttpErrorResponse) => {
        if (err.error && err.error.error) {
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
