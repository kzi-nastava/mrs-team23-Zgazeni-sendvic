import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Map as MapComponent, RouteMetrics } from '../map/map';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { RideTrackingWebSocketService } from '../service/ride-tracking-websocket.service';
import { RideTrackingUpdate } from '../models/ride-tracking.models';
import { Subscription } from 'rxjs';
import { AuthService } from '../service/auth.service';
import { PanicNotificationDTO } from '../models/panic.models';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-ride-tracking',
  imports: [MapComponent, MatButtonModule, FormsModule, MatSnackBarModule],
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
  userRole: string | null = null;
  private previousRideStatus: string | null = null;
  private rideEndedSubscription?: Subscription;
  private rideEndedAlertShown = false;

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
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  private showRideEndedNotification(): void {
    this.snackBar.open('Ride ended', 'OK', {
      duration: 7000,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

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
            if (!this.rideEndedAlertShown) {
              this.showRideEndedNotification();
              this.rideEndedAlertShown = true;
            }
            this.openRate();
          }
          this.previousRideStatus = displayRide?.status ?? null;

          this.currentRide = displayRide;
          if (displayRide) {
            this.updateMapView(displayRide);
          }
        }
      });

    this.rideEndedSubscription = this.rideTrackingService.getRideEndedNotifications()
      .subscribe(notification => {
        if (!notification) {
          return;
        }
        if (!this.rideEndedAlertShown && this.userRole !== 'DRIVER') {
          this.showRideEndedNotification();
          this.rideEndedAlertShown = true;
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
    const token = this.authService.getToken();
    if (!token) {
      console.error('No auth token available for ending ride');
      alert('Authentication error: Please login again');
      return;
    }

    console.log('Stop Ride clicked');
    this.http.put('http://localhost:8080/api/ride-end', { rideId: this.currentRide?.rideId, price: this.currentRide?.price, paid: true, ended: true }).subscribe({
      next: (response) => {
        console.log('Ride stopped successfully:', response);
      },
      error: (err) => {
        console.error('Error stopping ride:', err);
      }
    });
  }

  panicRide(): void {
    if (!this.currentRide?.rideId) {
      console.error('No current ride ID available');
      return;
    }
    console.log('Panic button clicked for ride:', this.currentRide.rideId);
    this.http.post<PanicNotificationDTO>(`http://localhost:8080/api/ride-PANIC/${this.currentRide.rideId}`, {}).subscribe({
      next: (response: PanicNotificationDTO) => {
        console.log('Panic notification sent successfully:', response);
        alert('Panic notification has been sent');
      },
      error: (err) => {
        console.error('Error sending panic notification:', err);
        alert('Error sending panic notification');
      }
    });
  }

  stopRideDriver(): void {
    if (!this.currentRide?.rideId) {
      console.error('No current ride ID available');
      return;
    }

    if (!this.currentRide) {
      console.error('No current ride data available');
      return;
    }

    const token = this.authService.getToken();
    console.log('Stop ride (driver) clicked for ride:', this.currentRide.rideId);
    console.log('Token available:', !!token, 'Token length:', token?.length);

    if (!token) {
      console.error('No auth token available for driver stop ride');
      alert('Authentication error: Please login again');
      return;
    }

    const routePoints = this.currentRide.route || [];

    // Prepare passed locations from route
    const passedLocations = routePoints.map(point => ({
      latitude: point.latitude,
      longitude: point.longitude
    }));

    // Backend expects at least one passed location for stop processing
    if (passedLocations.length === 0) {
      passedLocations.push({
        latitude: this.currentRide.currentLatitude,
        longitude: this.currentRide.currentLongitude
      });
    }

    // Send LocalDateTime-like string (without trailing Z) for backend compatibility
    const currentTime = new Date().toISOString().replace('Z', '');

    const rideStopRequest = {
      passedLocations,
      currentTime: currentTime
    };

    console.log('Stop ride request payload:', rideStopRequest);

    this.http.put(`http://localhost:8080/api/ride-tracking/stop/${this.currentRide.rideId}`, rideStopRequest).subscribe({
      next: (response) => {
        console.log('Ride stopped successfully:', response);
        alert('Ride has been stopped');
      },
      error: (err) => {
        console.error('Error stopping ride:', {
          status: err?.status,
          message: err?.message,
          error: err?.error,
          statusText: err?.statusText
        });

        // Extract detailed error message
        let errorMessage = 'Error stopping ride';
        if (err.error && err.error.message) {
          errorMessage = err.error.message;
        } else if (err.error && err.error.error) {
          errorMessage = err.error.error;
        } else if (err.statusText) {
          errorMessage = `Error: ${err.status} ${err.statusText}`;
        }

        alert(errorMessage);
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
    if (this.rideEndedSubscription) {
      this.rideEndedSubscription.unsubscribe();
    }
    if (this.userId) {
      this.rideTrackingService.disconnect(this.userId);
    }
  }
}
