import { Routes } from '@angular/router';
import { HorDriver } from './HOR-Driver/hor-driver';
import { Home } from './layout/home/home';
import { NavBar } from './layout/nav-bar/nav-bar';

export const routes: Routes = [
    { path: '', component: Home },
    { path: 'hor-driver', component: HorDriver },
    { path: '**', redirectTo: '' }
];
