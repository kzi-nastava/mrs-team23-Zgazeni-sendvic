import { Routes } from '@angular/router';
import { HorDriver } from './HOR-Driver/hor-driver';
import { Home } from './layout/home/home';
import { NavBar } from './layout/nav-bar/nav-bar';

import { Registration } from './registration/registration';
import { Login } from './login/login';
import { ForgotPassword } from './forgot-password/forgot-password';
import { ResetPassword } from './reset-password/reset-password';
export const routes: Routes = [
  { path: '', component: Home },
  { path: 'hor-driver', component: HorDriver },
  { path: '**', redirectTo: '' },
  { path: 'register', component: Registration },
  { path: 'login', component: Login },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword },
];
