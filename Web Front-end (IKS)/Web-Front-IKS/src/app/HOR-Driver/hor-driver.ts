import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'hor-driver',
  imports: [RouterModule, CommonModule],
  templateUrl: './hor-driver.html',
  styleUrl: './hor-driver.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HorDriver implements OnInit {

  rides: any[] = [];
  driverId: number | null = null;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.driverId = this.authService.getCurrentUserId();
    if (!this.driverId) {
      console.error('No logged in user found for ride history');
      return;
    }
    this.fetchRides();
  }

  fetchRides(): void {
    if (!this.driverId) {
      console.error('No logged in user found for ride history');
      return;
    }
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
            price: ride.price + ' RSD',
            canceled: ride.canceled ? "Canceled" : "Not Canceled",
            panic: ride.panic ? "Panic" : "Not Panic"
          }));
          this.rides.forEach(ride => {
            this.getAddressFromCoordinates(ride.origin).then(address => {
              ride.originAddress = address.split(',').slice(0, 2).join(',');
              this.cdr.markForCheck();
            });
            this.getAddressFromCoordinates(ride.destination).then(address => {
              ride.destinationAddress = address.split(',').slice(0, 2).join(',');
              this.cdr.markForCheck();
            });
          });
          console.log('Rides populated:', this.rides);
        },
        error: (err) => {
          console.error('Error fetching rides:', err);
          console.error('Error status:', err.status);
          console.error('Error message:', err.message);
        }
      });
  }

  private getAddressFromCoordinates(coords: { latitude: number; longitude: number }): Promise<string> {
    return this.http.get<any>(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${coords.longitude}&lon=${coords.latitude}`
    ).toPromise().then(
      response => {
        const address = response?.display_name || 'Unknown location';
        return String(address);
      },
      error => {
        console.error('Geocoding error:', error);
        return 'Unknown location';
      }
    );
  }
}
