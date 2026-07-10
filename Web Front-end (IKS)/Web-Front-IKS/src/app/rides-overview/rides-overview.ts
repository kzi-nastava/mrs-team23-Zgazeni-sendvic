import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../service/auth.service';
import { FormsModule } from '@angular/forms';


type RideLocation = { latitude: number; longitude: number };

type VehicleType = 'STANDARD' | 'VAN' | 'LUXURY';


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
  driverFirstName: string;
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
  driverEmail: string;
  driverFirstName: string;
  originNeedsLookup: boolean;

  destinationNeedsLookup: boolean;
};


@Component({
  selector: 'rides-overview',
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './rides-overview.html',
  styleUrl: './rides-overview.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RidesOverview implements OnInit {
  rides: RideOverviewRow[] = [];
  loading = false;
  error = '';

  searchDriverName = '';
  private activeGeocodeRequestId = 0;
  private geocodeLock = false;

  vehicleTypes: VehicleType[] = ['STANDARD', 'VAN', 'LUXURY'];
  selectedVehicleType: VehicleType = 'STANDARD';
  priceInput = '';
  pricingLoading = false;
  priceUpdateError = '';

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}


  async ngOnInit(): Promise<void> {
    this.fetchRides();
    await this.loadCurrentPrice(this.selectedVehicleType);
  }


  onSearch(): void {
    this.fetchRides(this.searchDriverName);
  }

  fetchRides(driverName?: string): void {
    this.loading = true;
    this.error = '';
    this.geocodeLock = true;


    const authToken = this.authService.getToken();
    if (!authToken) {
      this.error = 'You must be logged in as an admin to view this page.';
      this.loading = false;
      this.cdr.markForCheck();
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${authToken}` });

    const trimmed = (driverName ?? '').trim();
    const url = trimmed
      ? `http://localhost:8080/api/rides-overview?driverName=${encodeURIComponent(trimmed)}`
      : 'http://localhost:8080/api/rides-overview';

    const geocodeRequestId = ++this.activeGeocodeRequestId;

    this.http.get<{ activeRides: ActiveRideDTO[] }>(url, { headers })
      .subscribe({
        next: (response) => {
          const rawRides = response.activeRides ?? [];
          const filtered = this.filterActiveScheduled(rawRides);

          this.rides = filtered.map((ride, index) => this.toViewModel(ride, index));

          this.rides.forEach((ride, idx) => {
            // Add delay to respect Nominatim rate limit (1 req/sec)
            if (ride.originNeedsLookup && ride.origin) {
              setTimeout(() => {
                if (this.activeGeocodeRequestId !== geocodeRequestId) return;

                // Prevent race where a new search starts while old timeouts are still running.
                if (this.geocodeLock && this.activeGeocodeRequestId !== geocodeRequestId) return;

                this.getAddressFromCoordinates(ride.origin!).then(address => {
                  if (this.activeGeocodeRequestId !== geocodeRequestId) return;
                  ride.originAddress = this.shortenAddress(address);
                  this.cdr.markForCheck();
                }).catch(() => {
                  if (this.activeGeocodeRequestId !== geocodeRequestId) return;
                  ride.originAddress = this.formatCoords(ride.origin!);
                  this.cdr.markForCheck();
                });
              }, idx * 1100);
            }

            if (ride.destinationNeedsLookup && ride.destination) {
              setTimeout(() => {
                if (this.activeGeocodeRequestId !== geocodeRequestId) return;

                if (this.geocodeLock && this.activeGeocodeRequestId !== geocodeRequestId) return;

                this.getAddressFromCoordinates(ride.destination!).then(address => {
                  if (this.activeGeocodeRequestId !== geocodeRequestId) return;
                  ride.destinationAddress = this.shortenAddress(address);
                  this.cdr.markForCheck();
                }).catch(() => {
                  if (this.activeGeocodeRequestId !== geocodeRequestId) return;
                  ride.destinationAddress = this.formatCoords(ride.destination!);
                  this.cdr.markForCheck();
                });
              }, (idx * 1100) + 550);
            }


          });

          this.loading = false;
          this.geocodeLock = false;
          this.cdr.markForCheck();
        },
        error: (err) => {

          this.error = err.status === 403 
            ? 'Access denied. Admin privileges required.'
            : 'Failed to load rides overview.';
          this.loading = false;
          this.cdr.markForCheck();
        }
      });
  }


  private filterActiveScheduled(rides: ActiveRideDTO[]): ActiveRideDTO[] {
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
      driverEmail: ride.driverEmail,
      driverFirstName: ride.driverFirstName,
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

  private async loadCurrentPrice(vehicleType: VehicleType): Promise<void> {
    this.priceUpdateError = '';
    this.pricingLoading = true;
    this.cdr.markForCheck();

    const authToken = this.authService.getToken();
    if (!authToken) {
      this.priceUpdateError = 'You must be logged in as an admin to edit prices.';
      this.priceInput = '';
      this.pricingLoading = false;
      this.cdr.markForCheck();
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${authToken}` });

    try {
      const res = await firstValueFrom(
        this.http.put<any>('http://localhost:8080/api/ride-prices', {
          vehicleType,
          price: null,
        }, { headers })
      );

      const currentPrice = typeof res === 'number'
        ? res
        : (typeof res?.price === 'number' ? res.price : null);

      if (currentPrice === null) {
        this.priceUpdateError = 'Failed to read current price from server.';
        this.priceInput = '';
        return;
      }

      this.priceInput = String(currentPrice);
    } catch (err: any) {
      this.priceUpdateError = err?.status === 403
        ? 'Access denied. Admin privileges required.'
        : 'Failed to load current price.';
    } finally {
      this.pricingLoading = false;
      this.cdr.markForCheck();
    }
  }

  async onVehicleTypeChange(type: VehicleType): Promise<void> {
    this.selectedVehicleType = type;
    await this.loadCurrentPrice(type);
  }

  async setPrice(): Promise<void> {
    if (this.pricingLoading) return;

    this.priceUpdateError = '';

    const parsed = Number(this.priceInput);
    if (!Number.isFinite(parsed)) {
      this.priceUpdateError = 'Please enter a valid number.';
      return;
    }

    if (parsed < 0) {
      this.priceUpdateError = 'Price cannot be negative.';
      return;
    }

    const confirm = window.confirm(`Are you sure you want to set the price for '${this.selectedVehicleType}' to '${parsed}'?`);
    if (!confirm) {
      return;
    }

    const authToken = this.authService.getToken();
    if (!authToken) {
      this.priceUpdateError = 'You must be logged in as an admin to edit prices.';
      this.cdr.markForCheck();
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${authToken}` });

    this.pricingLoading = true;
    this.cdr.markForCheck();

    try {
      await firstValueFrom(
        this.http.put<any>('http://localhost:8080/api/ride-prices', {
          vehicleType: this.selectedVehicleType,
          price: parsed,
        }, { headers })
      );

      await this.loadCurrentPrice(this.selectedVehicleType);
      this.fetchRides(this.searchDriverName);

      alert(`the price for '${this.selectedVehicleType}' is set to '${parsed}'`);
    } catch (err: any) {
      this.priceUpdateError = err?.status === 403
        ? 'Access denied. Admin privileges required.'
        : 'Failed to update price.';

      this.pricingLoading = false;
      this.cdr.markForCheck();

      alert('failed to change the price');
    }
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

