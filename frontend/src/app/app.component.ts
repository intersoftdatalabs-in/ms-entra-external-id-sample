import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
imports: [RouterOutlet],
  // Add WelcomeComponent to imports if using standalone bootstrap
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {}