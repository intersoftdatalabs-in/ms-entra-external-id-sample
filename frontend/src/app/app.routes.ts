import { Routes } from '@angular/router';
import { AuthGuard } from './services/auth.guard';
import { LoginComponent } from './components/login/login.component';
import { WelcomeComponent } from './components/welcome/welcome.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'welcome', component: WelcomeComponent, canActivate: [AuthGuard] }
];