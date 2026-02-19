import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Map as MapComponent, RouteMetrics } from '../map/map';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { RideTrackingWebSocketService } from '../service/ride-tracking-websocket.service';
import { RideTrackingUpdate } from '../models/ride-tracking.models';
import { Subscription } from 'rxjs';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'app-ride-tracking',
  imports: [MapComponent, MatButtonModule, FormsModule],
  templateUrl: './ride-tracking.html',
  styleUrl: './ride-tracking.css',
})
export class RideTracking implements OnInit, OnDestroy {
  @ViewChild(MapComponent) mapComponent?: MapComponent;

  currentRide: RideTrackingUpdate | null = null;
  isConnected: boolean = false;
  private rideSubscription?: Subscription;
  private connectionSubscription?: Subscription;
  private userId: number | null = null;
  private userRole: string | null = null;
  private previousRideStatus: string | null = null;

  startingPoint = 'Bulevar oslobođenja 46, Novi Sad';
  destination = 'Trg slobode 1, Novi Sad';
  estimatedTime = '8 min';

  private readonly averageSpeedKmh = 45;
  private readonly geocodeCache = new Map<string, string>();
  private lastStartKey: string | null = null;
  private lastDestinationKey: string | null = null;

  showNoteForm = false;
  showRateForm = false;
  noteText = '';
  driverRating = 5;
  vehicleRating = 5;
  ratingComment = '';

  constructor(
    private rideTrackingService: RideTrackingWebSocketService,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userId = this.authService.getCurrentUserId();
    this.userRole = this.authService.getRole();
    if (!this.userId) {
      console.error('No logged in user found for ride tracking');
      return;
    }

    this.rideSubscription = this.rideTrackingService.getRideUpdates()
      .subscribe(rideUpdate => {
        if (rideUpdate) {
          const displayRide = this.selectRideToDisplay(rideUpdate);
          
          if (displayRide && this.previousRideStatus !== 'FINISHED' && 
              displayRide.status === 'FINISHED' && 
              this.userRole !== 'DRIVER') {
            this.openRate();
          }
          this.previousRideStatus = displayRide?.status ?? null;
          
          this.currentRide = displayRide;
          if (displayRide) {
            this.updateMapView(displayRide);
          }
        }
      });

    this.connectionSubscription = this.rideTrackingService.getConnectionStatus()
      .subscribe(status => {
        this.isConnected = status;
      });

    this.rideTrackingService.connect(this.userId);
  }

  private updateMapView(rideUpdate: RideTrackingUpdate): void {
    const currentLatitude = rideUpdate.currentLatitude;
    const currentLongitude = rideUpdate.currentLongitude;

    if (Number.isFinite(currentLatitude) && Number.isFinite(currentLongitude)) {
      const current: [number, number] = [currentLatitude, currentLongitude];
      const route = (rideUpdate.route ?? []).map((point) => [
        point.latitude,
        point.longitude,
      ] as [number, number]);
      const destination = route.length ? route[route.length - 1] : undefined;

      const startPoint = route.length ? route[0] : current;
      this.updateAddress(startPoint, 'start');
      if (destination) {
        this.updateAddress(destination, 'destination');
      } else {
        this.destination = 'Unknown';
      }

      this.mapComponent?.updateRideLocation(current, destination, route);
    }
  }

  onRouteMetrics(metrics: RouteMetrics): void {
    const durationSeconds = metrics.durationSeconds ?? this.calculateDuration(metrics.distanceMeters);
    this.estimatedTime = this.formatDuration(durationSeconds);
  }

  private calculateDuration(distanceMeters: number): number {
    const distanceKm = distanceMeters / 1000;
    const hours = distanceKm / this.averageSpeedKmh;
    return Math.max(0, Math.round(hours * 3600));
  }

  private formatDuration(totalSeconds: number): string {
    if (!Number.isFinite(totalSeconds) || totalSeconds <= 0) {
      return 'Unknown';
    }

    const totalMinutes = Math.max(1, Math.round(totalSeconds / 60));
    if (totalMinutes < 60) {
      return `${totalMinutes} min`;
    }

    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    return minutes ? `${hours} h ${minutes} min` : `${hours} h`;
  }

  private formatLatLng(point: [number, number]): string {
    return `${point[0].toFixed(5)}, ${point[1].toFixed(5)}`;
  }

  private updateAddress(point: [number, number], type: 'start' | 'destination'): void {
    const key = this.formatLatLng(point);
    const lastKey = type === 'start' ? this.lastStartKey : this.lastDestinationKey;

    if (key === lastKey) {
      return;
    }

    if (type === 'start') {
      this.lastStartKey = key;
    } else {
      this.lastDestinationKey = key;
    }

    const cached = this.geocodeCache.get(key);
    if (cached) {
      this.setAddress(type, cached);
      return;
    }

    const [lat, lon] = point;
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lon}`;
    this.http.get<{ display_name?: string }>(url).subscribe({
      next: (response) => {
        const city = response.display_name ? response.display_name.split(',')[4]?.trim() : null;
        const name = response.display_name ? response.display_name.split(',').slice(0, 2).concat(city ? [city] : []).join(',') : key;
        this.geocodeCache.set(key, name);
        this.setAddress(type, name);
      },
      error: () => {
        this.setAddress(type, key);
      }
    });
  }

  private setAddress(type: 'start' | 'destination', value: string): void {
    if (type === 'start') {
      this.startingPoint = value;
    } else {
      this.destination = value;
    }
  }

  stopRide(): void {
    console.log('Stop Ride clicked');
    this.http.put('http://localhost:8080/api/ride-end', { rideId: this.currentRide?.rideId,price: this.currentRide?.price, paid: true, ended: true }).subscribe({
      next: (response) => {
        console.log('Ride stopped successfully:', response);
      },
      error: (err) => {
        console.error('Error stopping ride:', err);
      }
    }); 
  }

  isDriver(): boolean {
    return this.userRole === 'DRIVER';
  }

  private selectRideToDisplay(rideUpdate: RideTrackingUpdate): RideTrackingUpdate | null {
    if (rideUpdate.status === 'CANCELED') {
      return null;
    }

    if (['ACTIVE', 'SCHEDULED', 'FINISHED'].includes(rideUpdate.status)) {
      return rideUpdate;
    }

    return null;
  }

  openNote(): void {
    this.showNoteForm = true;
    this.showRateForm = false;
  }

  openRate(): void {
    this.showRateForm = true;
    this.showNoteForm = false;
  }

  closeNoteForm(): void {
    this.showNoteForm = false;
    this.noteText = '';
  }

  closeRateForm(): void {
    this.showRateForm = false;
    this.driverRating = 5;
    this.vehicleRating = 5;
    this.ratingComment = '';
  }

  sendNote(): void {
    console.log('Note sent:', this.noteText);
    this.closeNoteForm();
  }

  sendRating(): void {
    if (!this.currentRide) {
      console.error('No current ride to rate');
      return;
    }
    if (!this.userId) {
      console.error('No logged in user found for rating');
      return;
    }
    this.http.post(`http://localhost:8080/api/ride-driver-rating/${this.userId}`, {
      userId: this.userId,
      rideId: this.currentRide?.rideId,
      driverRating: this.driverRating,
      vehicleRating: this.vehicleRating,
      comment: this.ratingComment
    }).subscribe({
      next: (response) => {        
      console.log('Rating sent successfully:', response);
      console.log('Rating:', {
      driver: this.driverRating,
      vehicle: this.vehicleRating,
      comment: this.ratingComment,
    });
      },
      error: (err) => {
        console.error('Error sending rating:', err);
        this.closeRateForm();
      }
    });
    
    this.closeRateForm();
  }

  ngOnDestroy(): void {
    if(this.rideSubscription) {
      this.rideSubscription.unsubscribe();
    }
    if(this.connectionSubscription) {
      this.connectionSubscription.unsubscribe();
    }
    if (this.userId) {
      this.rideTrackingService.disconnect(this.userId);
    }
  }
}
