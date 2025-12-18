import { Routes } from '@angular/router';
import { Home } from './home/home';
import { Registration } from './registration/registration';
import { Login } from './login/login';
import { ForgotPassword } from './forgot-password/forgot-password';
import { ResetPassword } from './reset-password/reset-password';
export const routes: Routes = [
  { path: '', component: Home },
  { path: 'register', component: Registration },
  { path: 'login', component: Login },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword },
];
