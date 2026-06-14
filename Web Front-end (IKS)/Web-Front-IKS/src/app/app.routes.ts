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
import { RideOrder } from './ride-order/ride-order';
import { HORAdmin } from './hor-admin/hor-admin';
import {HORUser} from "./hor-user/hor-user";
import { DetailedHorAdmin } from './hor-admin/detailed-hor-admin/detailed-hor-admin';
import { DetailedHorUser } from './hor-user/detailed-hor-user/detailed-hor-user';
import { PanicNotifications } from './panic-notifications/panic-notifications';
import { FavoriteRoutes } from './favorite-routes/favorite-routes';
import { RidesOverview } from './rides-overview/rides-overview';
import { BanAccount } from './admin/ban-account/ban-account';
import { RegisterDriver } from './driver/register-driver/register-driver';
import { ActivateDriver } from './driver/activate-driver/activate-driver';
import { RegisterVehicle } from './driver/register-vehicle/register-vehicle';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'profile', component: ProfileCard},
  { path: 'profile/edit', component: ProfileEdit },
  { path: 'approve', component: DriverAcceptEdit},
  { path: 'activate-driver', component: ActivateDriver },
  { path: 'hor-driver', component: HorDriver },
  { path: 'future-rides', component: FutureRides },
  { path: 'ride-order', component: RideOrder },
  { path: 'favorite-routes', component: FavoriteRoutes },
  { path: 'admin/ban-account', component: BanAccount },
  { path: 'register-driver', component: RegisterDriver },
  { path: 'register-vehicle', component: RegisterVehicle},
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
  { path: 'rides-overview', component: RidesOverview },
  { path: '**', redirectTo: '' },
];
