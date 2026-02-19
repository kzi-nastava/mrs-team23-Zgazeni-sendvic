import { Component, ChangeDetectorRef } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../service/auth.service';
import { DriverService } from '../../service/driver.service';
import { RouteEstimationService } from '../../service/route.estimation.serivce';

@Component({
  selector: 'app-nav-bar',
  imports: [RouterModule, CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBar {
  constructor(
    private authService: AuthService,
    private driverService: DriverService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    public panelService: RouteEstimationService
  ) {}

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.authService.clearToken();
        this.router.navigate(['/login']);
      },
      error: err => {
        console.error('Logout failed:', err);
      }
    });
  }

  toggleDriverStatus(): void {
    const nextStatus = !this.authService.getDriverActive();
    this.driverService.requestDriverDeactivation(nextStatus).subscribe({
      next: () => {
        this.authService.setDriverActive(nextStatus);
        this.cdr.markForCheck();
      },
      error: err => {
        console.error('Driver status update failed:', err);
      }
    });
  }

  getDriverStatusText(): string {
    return this.authService.getDriverActive() ? 'Active' : 'Inactive';
  }

  getDriverToggleLabel(): string {
    return this.authService.getDriverActive() ? 'Deactivate' : 'Activate';
  }

  getDriverActive(): boolean {
    return this.authService.getDriverActive();
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
}
