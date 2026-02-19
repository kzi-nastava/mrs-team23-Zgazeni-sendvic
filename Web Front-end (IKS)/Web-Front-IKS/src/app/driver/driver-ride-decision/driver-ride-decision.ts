import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { DriverService } from '../../service/driver.service';
import { Inject } from '@angular/core';

@Component({
  selector: 'app-driver-ride-decision',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  templateUrl: './driver-ride-decision.html',
  styleUrl: './driver-ride-decision.css',
})
export class DriverRideDecision implements OnInit {

  token!: string;
  loading = true;
  error?: string;

  ride?: {
    start: string;
    destination: string;
    price: number;
  };

  constructor(
    private route: ActivatedRoute,
    @Inject(DriverService) private driverService: DriverService
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token')!;

    this.driverService.getRideDecisionInfo(this.token).subscribe({
      next: (ride: { start: string; destination: string; price: number; } | undefined) => {
        this.ride = ride;
        this.loading = false;
      },
      error: () => {
        this.error = 'Invalid or expired link';
        this.loading = false;
      }
    });
  }

  accept() {
    this.driverService.acceptRide(this.token).subscribe({
      next: () => alert('Ride accepted'),
      error: () => alert('Failed to accept ride')
    });
  }

  reject() {
    this.driverService.rejectRide(this.token).subscribe({
      next: () => alert('Ride rejected'),
      error: () => alert('Failed to reject ride')
    });
  }
}
