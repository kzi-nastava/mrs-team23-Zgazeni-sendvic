import { Routes } from '@angular/router';
import { Home } from './home/home';
import { Registration } from './registration/registration';
import { Login } from './login/login';
export const routes: Routes = [
  { path: '', component: Home },
  { path: 'register', component: Registration },
  { path: 'login', component: Login },
];
