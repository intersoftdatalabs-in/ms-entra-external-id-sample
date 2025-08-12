import { Component , OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
/*
@Component({
  selector: 'app-ssowelcome',
  standalone: true,
  templateUrl: './ssowelcome.component.html',
  styleUrls: ['./ssowelcome.component.css']
})
export class SsoWelcomeComponent {
  email: string = '';
  constructor(private router: Router, private authService: AuthService) {
    const nav = this.router.getCurrentNavigation();
    this.email = nav?.extras.state?.['email'] || '';
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

*/

import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-ssowelcome',
  standalone: true,
  templateUrl: './ssowelcome.component.html',
  styleUrls: ['./ssowelcome.component.css']
})
export class SsoWelcomeComponent  {

  email: string = '';

  constructor(private route: ActivatedRoute, private router: Router, private authService: AuthService) {
    //alert("SsoWelcomeComponent.constructor");
  }

  ngOnInit() {
    //alert("SsoWelcomeComponent.ngOnInit");
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const state = params['state'];
      const sessionState = params['session_state'];
      const userid = params['userid'];
      const error = params['error'];

      sessionStorage.setItem('sessionAuthUserId', userid);
      this.email = sessionStorage.getItem('sessionAuthUserId') || '';

      if (code) {
        // Send code to backend to exchange for tokens
        this.sendCodeToBackend(code, state, sessionState, userid, error);
      }
    });
  }

  sendCodeToBackend(code: string, state: string, sessionState: string, email: string, error: string) {
    console.log("code: "+code);
    console.log("state: "+state);
    console.log("sessionState: "+sessionState);
    console.log("email: "+email);
    console.log("error: "+error);
    // Call your Spring Boot API to exchange code for tokens
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

