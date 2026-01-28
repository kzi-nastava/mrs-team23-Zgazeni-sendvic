import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'hor-driver',
  imports: [RouterModule, CommonModule],
  templateUrl: './hor-driver.html',
  styleUrl: './hor-driver.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HorDriver implements OnInit {

  rides: any[] = [];
  driverId = 100;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.fetchRides();
  }

  fetchRides(): void {
    this.http.get<{ rides: any[] }>(`http://localhost:8080/api/history-of-rides/${this.driverId}`)
      .subscribe({
        next: (response) => {
          console.log('Response received:', response);
          this.rides = response.rides.map(ride => ({
            ...ride,
            date: new Date(ride.departureTime).toLocaleDateString(),
            timeStart: new Date(ride.departureTime).toLocaleTimeString(),
            timeEnd: new Date(ride.arrivalTime).toLocaleTimeString(),
            origin: ride.origin,
            destination: ride.destination,
            price: ride.price,
            canceled: ride.canceled,
            panic: ride.panic
          }));
          console.log('Rides populated:', this.rides);
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error fetching rides:', err);
          console.error('Error status:', err.status);
          console.error('Error message:', err.message);
        }
      });
  }
}
