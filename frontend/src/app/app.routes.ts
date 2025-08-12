import { Routes } from '@angular/router';
import { AuthGuard } from './services/auth.guard';
import { LoginComponent } from './components/login/login.component';
import { WelcomeComponent } from './components/welcome/welcome.component';
import { SsoWelcomeComponent } from './components/ssowelcome/ssowelcome.component';
import { AuthCallbackComponent } from './components/auth-callback/auth-callback.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'auth/callback', component: AuthCallbackComponent },
  { path: 'welcome', component: WelcomeComponent, canActivate: [AuthGuard] },
  { path: 'ssowelcome', component: SsoWelcomeComponent }
];