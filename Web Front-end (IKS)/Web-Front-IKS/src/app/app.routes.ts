import { Routes } from '@angular/router';
import { UserProfile } from './profiles/user-profile/user-profile';
import { DriverProfile } from './profiles/driver-profile/driver-profile';
import { AdminProfile } from './profiles/admin-profile/admin-profile';
import { Home } from './layout/home/home';
import { HorDriver } from './HOR-Driver/hor-driver';

import { Registration } from './registration/registration';
import { Login } from './login/login';
import { ForgotPassword } from './forgot-password/forgot-password';
import { ResetPassword } from './reset-password/reset-password';
import { ConfirmAccount } from './confirm-account/confirm-account';
import { RouteEstimationPanel } from './route-estimation-panel/route-estimation-panel';


export const routes: Routes = [
  { path: '', component: Home },
  { path: 'profile/user', component: UserProfile },
  { path: 'profile/driver', component: DriverProfile },
  { path: 'profile/admin', component: AdminProfile },
  { path: 'hor-driver', component: HorDriver },
  { path: 'register', component: Registration },
  { path: 'login', component: Login },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword },
  { path: 'api/auth/confirm-account', component: ConfirmAccount },
  { path: 'route-estimation', component: RouteEstimationPanel },
  { path: '**', redirectTo: '' },
];
