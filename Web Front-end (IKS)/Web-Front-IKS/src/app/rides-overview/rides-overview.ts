import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../service/auth.service';

type RideLocation = { latitude: number; longitude: number };

interface ActiveRideDTO {
  id: number;
  origin: RideLocation;
  destination: RideLocation;
  departureTime: string | null;
  arrivalTime: string;
  panic: boolean;
  status: string;
  price: number;
  driverEmail: string;
  date: string;
}

type RideOverviewRow = {
  trackId: string;
  date: string;
  timeStart: string;
  timeEnd: string;
  origin: RideLocation | null;
  destination: RideLocation | null;
  originAddress: string;
  destinationAddress: string;
  price: string;
  status: string;
  driverLabel: string;
  originNeedsLookup: boolean;
  destinationNeedsLookup: boolean;
};

@Component({
  selector: 'rides-overview',
  imports: [RouterModule, CommonModule],
  templateUrl: './rides-overview.html',
  styleUrl: './rides-overview.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RidesOverview implements OnInit {
  rides: RideOverviewRow[] = [];
  loading = false;
  error = '';

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.fetchRides();
  }

  fetchRides(): void {
    this.loading = true;
    this.error = '';

    const authToken = this.authService.getToken();
    if (!authToken) {
      this.error = 'You must be logged in as an admin to view this page.';
      this.loading = false;
      this.cdr.markForCheck();
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${authToken}` });

    this.http.get<{ activeRides: ActiveRideDTO[] }>('http://localhost:8080/api/rides-overview', { headers })
      .subscribe({
        next: (response) => {
          console.log('Rides overview response:', response);
          console.log('Response type:', typeof response);
          console.log('Is array?', Array.isArray(response));
          
          const rawRides = response.activeRides ?? [];

          console.log('Raw rides:', rawRides);
          console.log('Raw rides length:', rawRides.length);
          
          const filtered = this.filterActiveScheduled(rawRides);
          console.log('Filtered rides:', filtered);
          console.log('Filtered rides length:', filtered.length);
          
          this.rides = filtered.map((ride, index) => this.toViewModel(ride, index));
          console.log('View model rides:', this.rides);

          this.rides.forEach((ride, idx) => {
            // Add delay to respect Nominatim rate limit (1 req/sec)
            if (ride.originNeedsLookup && ride.origin) {
              console.log('Fetching address for origin:', ride.origin);
              setTimeout(() => {
                this.getAddressFromCoordinates(ride.origin!).then(address => {
                  console.log('Received origin address:', address);
                  ride.originAddress = this.shortenAddress(address);
                  this.cdr.markForCheck();
                }).catch(err => {
                  console.error('Failed to geocode origin:', err);
                  ride.originAddress = this.formatCoords(ride.origin!);
                  this.cdr.markForCheck();
                });
              }, idx * 1100);
            }
            if (ride.destinationNeedsLookup && ride.destination) {
              console.log('Fetching address for destination:', ride.destination);
              setTimeout(() => {
                this.getAddressFromCoordinates(ride.destination!).then(address => {
                  console.log('Received destination address:', address);
                  ride.destinationAddress = this.shortenAddress(address);
                  this.cdr.markForCheck();
                }).catch(err => {
                  console.error('Failed to geocode destination:', err);
                  ride.destinationAddress = this.formatCoords(ride.destination!);
                  this.cdr.markForCheck();
                });
              }, (idx * 1100) + 550);
            }
          });

          this.loading = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error fetching rides overview:', err);
          this.error = err.status === 403 
            ? 'Access denied. Admin privileges required.'
            : 'Failed to load rides overview.';
          this.loading = false;
          this.cdr.markForCheck();
        }
      });
  }

  private filterActiveScheduled(rides: ActiveRideDTO[]): ActiveRideDTO[] {
    // ActiveRideDTO always has a 'status' field
    return rides.filter(ride => {
      const rawStatus = String(ride.status ?? '').toUpperCase();
      return rawStatus === 'ACTIVE' || rawStatus === 'SCHEDULED';
    });
  }

  private toViewModel(ride: ActiveRideDTO, index: number): RideOverviewRow {
    // ActiveRideDTO fields: id, origin, destination, departureTime, arrivalTime, panic, status, price, driverEmail, date
    const departureTime = this.parseDate(ride.departureTime);
    const arrivalTime = this.parseDate(ride.arrivalTime);
    
    const origin = ride.origin;
    const destination = ride.destination;

    const dateStr = ride.date ?? (departureTime ? departureTime.toLocaleDateString() : '-');

    return {
      trackId: String(ride.id),
      date: dateStr,
      timeStart: departureTime ? departureTime.toLocaleTimeString() : '-',
      timeEnd: arrivalTime ? arrivalTime.toLocaleTimeString() : '-',
      origin,
      destination,
      originAddress: origin ? this.formatCoords(origin) : 'Unknown',
      destinationAddress: destination ? this.formatCoords(destination) : 'Unknown',
      price: `${ride.price} RSD`,
      status: this.formatStatus(ride.status),
      driverLabel: ride.driverEmail,
      originNeedsLookup: Boolean(origin),
      destinationNeedsLookup: Boolean(destination),
    };
  }

  private parseDate(value: unknown): Date | null {
    if (value instanceof Date) return value;
    if (typeof value === 'string' || typeof value === 'number') {
      const parsed = new Date(value);
      return Number.isNaN(parsed.getTime()) ? null : parsed;
    }
    return null;
  }

  private formatStatus(value: unknown): string {
    if (!value) return '-';
    const text = String(value).toLowerCase();
    return text.charAt(0).toUpperCase() + text.slice(1);
  }

  private formatCoords(coords: RideLocation): string {
    return `${coords.latitude.toFixed(5)}, ${coords.longitude.toFixed(5)}`;
  }

  private shortenAddress(address: string): string {
    return address.split(',').slice(0, 2).join(',');
  }

  private async getAddressFromCoordinates(coords: RideLocation): Promise<string> {
    try {
      const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${coords.latitude}&lon=${coords.longitude}`;
      console.log('Geocoding URL:', url);
      
      const headers = new HttpHeaders({
        'User-Agent': 'RidesOverviewApp/1.0'
      });
      
      const response = await firstValueFrom(
        this.http.get<{ display_name?: string }>(url, { headers })
      );
      
      console.log('Geocoding response:', response);
      const address = String(response?.display_name ?? 'Unknown location');
      return address;
    } catch (error) {
      console.error('Geocoding error:', error);
      return 'Unknown location';
    }
  }
}
