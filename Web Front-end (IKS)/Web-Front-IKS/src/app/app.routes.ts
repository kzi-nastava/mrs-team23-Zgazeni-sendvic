import { Routes } from '@angular/router';
import { UserProfile } from './profiles/user-profile/user-profile';
import { DriverProfile } from './profiles/driver-profile/driver-profile';
import { AdminProfile } from './profiles/admin-profile/admin-profile';
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
import { HORAdmin } from './hor-admin/hor-admin';
import {HORUser} from "./hor-user/hor-user";
import { DetailedHorAdmin } from './hor-admin/detailed-hor-admin/detailed-hor-admin';
import { DetailedHorUser } from './hor-user/detailed-hor-user/detailed-hor-user';
import { PanicNotifications } from './panic-notifications/panic-notifications'
export const routes: Routes = [
  { path: '', component: Home },
  { path: 'profile/user', component: UserProfile },
  { path: 'profile/driver', component: DriverProfile },
  { path: 'profile/admin', component: AdminProfile },
  { path: 'hor-driver', component: HorDriver },
  { path: 'future-rides', component: FutureRides },
  { path: 'ride-tracking', component: RideTracking },
  { path: 'register', component: Registration },
  { path: 'login', component: Login },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword },
  { path: 'api/auth/confirm-account', component: ConfirmAccount },
  { path: 'route-estimation', component: RouteEstimationPanel },
  {path: 'hor-admin', component: HORAdmin},
  {path: 'hor-admin/detailed/:id', component: DetailedHorAdmin},
  {path: 'hor-user', component: HORUser},
  {path: 'hor-user/detailed/:id', component: DetailedHorUser},
  {path: 'panic-notifications', component: PanicNotifications},
  { path: '**', redirectTo: '' },
];
