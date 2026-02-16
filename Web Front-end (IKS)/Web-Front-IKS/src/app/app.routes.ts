import { Routes } from '@angular/router';
import { Home } from './layout/home/home';
import { HorDriver } from './HOR-Driver/hor-driver';
import { RideTracking } from './ride-tracking/ride-tracking';
import { FutureRides } from './future-rides/future-rides';
import { Registration } from './registration/registration';
import { Login } from './login/login';
import { ForgotPassword } from './forgot-password/forgot-password';
import { ResetPassword } from './reset-password/reset-password';
import { ConfirmAccount } from './confirm-account/confirm-account';
import { RouteEstimationPanel } from './route-estimation-panel/route-estimation-panel';
import { ProfileCard } from './profiles/profile-card/profile-card';
import { ProfileEdit } from './profiles/profile-edit/profile-edit';
import { DriverAcceptEdit } from './profiles/driver-accept-edit/driver-accept-edit';


export const routes: Routes = [
  { path: '', component: Home },
  { path: 'profile', component: ProfileCard},
  { path: 'profile/edit', component: ProfileEdit },
  { path: 'approve', component: DriverAcceptEdit},
  { path: 'hor-driver', component: HorDriver },
  { path: 'future-rides', component: FutureRides },
  { path: 'ride-tracking', component: RideTracking },
  { path: 'register', component: Registration },
  { path: 'login', component: Login },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword },
  { path: 'api/auth/confirm-account', component: ConfirmAccount },
  { path: 'route-estimation', component: RouteEstimationPanel },
  { path: '**', redirectTo: '' },
];
