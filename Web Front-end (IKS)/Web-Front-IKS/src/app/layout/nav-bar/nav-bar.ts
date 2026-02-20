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
      error: (err: any) => {
        console.error('Logout failed:', err);

        // Get the error message from the backend response
        let errorMessage = 'Logout failed. Please try again.';

        if (err.error && typeof err.error === 'string') {
          errorMessage = err.error;
        } else if (err.error && err.error.message) {
          errorMessage = err.error.message;
        } else if (err.message) {
          errorMessage = err.message;
        }

        // Check if it's a driver availability issue
        if (err.status === 400 && errorMessage.includes('available')) {
          errorMessage = 'You must set your status to "Not Available" before logging out.';
        }

        alert(errorMessage);
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
        if (err?.status === 401) {
          alert('Session expired or unauthorized. Please log in again.');
          return;
        }
        alert('Unable to update driver status. Please try again.');
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

  navigateToHOR(): void {
    if (this.isAdmin()) {
      this.router.navigate(['/hor-admin']);
    } else if (this.isUser()) {
      this.router.navigate(['/hor-user']);
    }
  }

  isLoggedIn(): boolean {
    return this.authService.isAuthenticated();
  }
}
