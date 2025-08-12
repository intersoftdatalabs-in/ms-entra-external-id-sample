import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-welcome',
  standalone: true,
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent {
  email: string = '';
  applications: string[] = [];
  constructor(private router: Router, private authService: AuthService) {
    const nav = this.router.getCurrentNavigation();
    this.email = nav?.extras.state?.['email'] || '';
    this.applications = nav?.extras.state?.['applications'] || [];
  }

  signOut() {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['']);
      },
      error: () => {
        this.router.navigate(['']);
      }
    });
  }
}
