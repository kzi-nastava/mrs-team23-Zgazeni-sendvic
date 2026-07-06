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
import { HORUser } from "./hor-user/hor-user";
import { DetailedHorAdmin } from './hor-admin/detailed-hor-admin/detailed-hor-admin';
import { DetailedHorUser } from './hor-user/detailed-hor-user/detailed-hor-user';
import { PanicNotifications } from './panic-notifications/panic-notifications';
import { FavoriteRoutes } from './favorite-routes/favorite-routes';
import { RidesOverview } from './rides-overview/rides-overview';
import { BanAccount } from './admin/ban-account/ban-account';
import { RegisterDriver } from './driver/register-driver/register-driver';
import { ActivateDriver } from './driver/activate-driver/activate-driver';
import { RegisterVehicle } from './driver/register-vehicle/register-vehicle';
import { RidesReport } from './rides-report/rides-report';
import { AuthGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: '', component: Home },

  // AUTH (public)
  { path: 'register', component: Registration },
  { path: 'login', component: Login },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'reset-password', component: ResetPassword },
  { path: 'api/auth/confirm-account', component: ConfirmAccount },

  // USER AREA (protected)
  {
    path: 'profile',
    component: ProfileCard,
    canActivate: [AuthGuard],
    data: { roles: ['USER', 'DRIVER', 'ADMIN'] }
  },
  {
    path: 'profile/edit',
    component: ProfileEdit,
    canActivate: [AuthGuard],
    data: { roles: ['USER', 'DRIVER', 'ADMIN'] }
  },

  // DRIVER
  {
    path: 'hor-driver',
    component: HorDriver,
    canActivate: [AuthGuard],
    data: { roles: ['DRIVER'] }
  },

  {
    path: 'ride-tracking',
    component: RideTracking,
    canActivate: [AuthGuard],
    data: { roles: ['DRIVER', 'USER'] }
  },

  {
    path: 'future-rides',
    component: FutureRides,
    canActivate: [AuthGuard],
    data: { roles: ['USER', 'DRIVER'] }
  },

  // USER FEATURES
  {
    path: 'ride-order',
    component: RideOrder,
    canActivate: [AuthGuard],
    data: { roles: ['USER'] }
  },

  {
    path: 'favorite-routes',
    component: FavoriteRoutes,
    canActivate: [AuthGuard],
    data: { roles: ['USER'] }
  },

  // REPORT
  {
    path: 'rides-report',
    component: RidesReport,
    canActivate: [AuthGuard],
    data: { roles: ['USER', 'DRIVER', 'ADMIN'] }
  },

  // ADMIN AREA
  {
    path: 'admin/ban-account',
    component: BanAccount,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  {
    path: 'hor-admin',
    component: HORAdmin,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  {
    path: 'hor-admin/detailed/:id',
    component: DetailedHorAdmin,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  // USER HISTORY
  {
    path: 'hor-user',
    component: HORUser,
    canActivate: [AuthGuard],
    data: { roles: ['USER'] }
  },

  {
    path: 'hor-user/detailed/:id',
    component: DetailedHorUser,
    canActivate: [AuthGuard],
    data: { roles: ['USER'] }
  },

  // DRIVER SETUP
  {
    path: 'approve',
    component: DriverAcceptEdit,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  {
    path: 'activate-driver',
    component: ActivateDriver,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  {
    path: 'register-driver',
    component: RegisterDriver,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  {
    path: 'register-vehicle',
    component: RegisterVehicle,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  // SHARED
  {
    path: 'route-estimation',
    component: RouteEstimationPanel,
    canActivate: [AuthGuard],
    data: { roles: ['USER', 'DRIVER'] }
  },

  {
    path: 'rides-overview',
    component: RidesOverview,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },

  {
    path: 'panic-notifications',
    component: PanicNotifications,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN', 'DRIVER'] }
  },

  // FALLBACK
  { path: '**', redirectTo: '' },
];
