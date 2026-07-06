import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

@Component({
  selector: 'app-rides-report',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
  ],
  templateUrl: './rides-report.html',
  styleUrl: './rides-report.css'
})
export class RidesReport {

  fromDate: Date | null = null;
  toDate: Date | null = null;

  loading = signal(false);

  // ===== MOCK DATA (replace later with API) =====
  rides = signal<any[]>([
    {
      rideId: 1,
      startTime: new Date(),
      driverName: 'John Doe',
      distanceKm: 12.4,
      durationMinutes: 22,
      totalPrice: 9.5,
      status: 'COMPLETED'
    },
    {
      rideId: 2,
      startTime: new Date(),
      driverName: 'Jane Smith',
      distanceKm: 5.2,
      durationMinutes: 10,
      totalPrice: 4.1,
      status: 'COMPLETED'
    },
    {
      rideId: 3,
      startTime: new Date(),
      driverName: 'Mike Johnson',
      distanceKm: 18.7,
      durationMinutes: 35,
      totalPrice: 15.2,
      status: 'CANCELED'
    }
  ]);

  // ===== SUMMARY (computed from rides) =====
  get totalRides() {
    return this.rides().length;
  }

  get totalDistance() {
    return this.rides().reduce((sum, r) => sum + r.distanceKm, 0);
  }

  get totalRevenue() {
    return this.rides().reduce((sum, r) => sum + r.totalPrice, 0);
  }

  get avgDuration() {
    if (!this.rides().length) return 0;
    return this.rides().reduce((sum, r) => sum + r.durationMinutes, 0) / this.rides().length;
  }

  // ===== FILTER (mock behavior) =====
  applyFilters() {
    // later: call backend
    console.log('Filtering from', this.fromDate, 'to', this.toDate);
  }

  clearFilters() {
    this.fromDate = null;
    this.toDate = null;
  }

  // ===== CHART DATA =====
  get distanceChartData() {
    return {
      labels: this.rides().map(r => `Ride ${r.rideId}`),
      datasets: [{
        label: 'Distance (km)',
        data: this.rides().map(r => r.distanceKm)
      }]
    };
  }

  get revenueChartData() {
    return {
      labels: this.rides().map(r => `Ride ${r.rideId}`),
      datasets: [{
        label: 'Revenue (€)',
        data: this.rides().map(r => r.totalPrice)
      }]
    };
  }
}