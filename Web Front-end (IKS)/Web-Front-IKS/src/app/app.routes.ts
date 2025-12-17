import { Routes } from '@angular/router';
import { Home } from './home/home';
import { Registration } from './registration/registration';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'register', component: Registration },
];
