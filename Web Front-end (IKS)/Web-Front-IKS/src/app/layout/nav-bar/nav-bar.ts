import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../service/auth.service';
import { RouteEstimationService } from '../../service/route.estimation.serivce';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-nav-bar',
  imports: [RouterModule, CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBar {
  constructor(private authService: AuthService, private router: Router, public panelService: RouteEstimationService, private http: HttpClient) {}

  logout() {
    this.authService.clearToken();
    this.router.navigate(['/login']);
  }

  isHomePage(): boolean {
    return this.router.url === '/';
  }

  isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

  isDriver(): boolean {
    return this.authService.getRole() === 'DRIVER';
  }

  isUser(): boolean {
    return this.authService.getRole() === 'USER';
  }

  isLoggedIn(): boolean {
    return this.authService.isAuthenticated();
  }

  startRide() {
    return this.http.put(`http://localhost:8080/api/ride-start`, {});
  }
}
