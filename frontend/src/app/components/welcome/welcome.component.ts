import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-welcome',
  standalone: true,
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent {
  username: string = '';
  constructor(private router: Router) {
    const nav = this.router.getCurrentNavigation();
    this.username = nav?.extras.state?.['username'] || '';
  }
}
