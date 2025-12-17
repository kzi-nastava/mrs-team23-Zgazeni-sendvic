import { Routes } from '@angular/router';
import { UserProfile } from './profiles/user-profile/user-profile';
import { DriverProfile } from './profiles/driver-profile/driver-profile';
import { AdminProfile } from './profiles/admin-profile/admin-profile';
import { Home } from './layout/home/home';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'profile/user', component: UserProfile },
  { path: 'profile/driver', component: DriverProfile },
  { path: 'profile/admin', component: AdminProfile },
  { path: '**', redirectTo: '' },
];
