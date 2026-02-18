import { Component, ViewChild, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormArray, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Map } from '../map/map';
import { RideService } from '../service/ride.service';
import { VehicleType } from '../models/ride-request.model';
import { RouteDTO } from '../models/route.dto';

@Component({
  selector: 'app-ride-order',
  templateUrl: './ride-order.html',
  styleUrls: ['./ride-order.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Map]
})
export class RideOrder implements OnInit {

  @ViewChild(Map) mapComponent!: Map;

  pickupCoords?: [number, number];
  destinationCoords?: [number, number];

  private clickCount = 0;

  vehicleTypes = Object.values(VehicleType);
  rideForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private rideService: RideService,
    private router: Router
  ) {
    this.rideForm = this.fb.group({
      locations: this.fb.array([]),
      vehicleType: [VehicleType.STANDARD, Validators.required],
      babiesAllowed: [false],
      petsAllowed: [false],
      scheduledTime: [null],
      invitedPassengerEmails: [[]],   // IMPORTANT: match backend naming
      estimatedDistanceKm: [0]
    });
  }

  ngOnInit(): void {
    // default 2 locations
    this.addLocation(); // start
    this.addLocation(); // destination

    // if navigated from favorites
    const nav = this.router.getCurrentNavigation();
    const selectedRoute = (nav?.extras?.state as any)?.selectedRoute as RouteDTO | undefined;

    // Also support refresh (router navigation state is lost on full refresh)
    // So check history.state too:
    const fallbackRoute = (history.state?.selectedRoute as RouteDTO | undefined);

    const route = selectedRoute ?? fallbackRoute;
    if (route) {
      this.applyFavoriteRoute(route);
    }
  }

  get locations(): FormArray {
    return this.rideForm.get('locations') as FormArray;
  }

  private newLocationGroup(address = '', lat = 0, lng = 0) {
    return this.fb.group({
      address: [address, Validators.required],
      latitude: [lat, Validators.required],
      longitude: [lng, Validators.required]
    });
  }

  addLocation() {
    this.locations.push(this.newLocationGroup());
  }

  removeLocation(index: number) {
    if (this.locations.length > 2) {
      this.locations.removeAt(index);
    }
  }

  private applyFavoriteRoute(route: RouteDTO) {
    const locs = [route.start, ...(route.midPoints ?? []), route.destination];

    // clear & rebuild
    while (this.locations.length) this.locations.removeAt(0);
    for (const l of locs) {
      this.locations.push(this.newLocationGroup(
        '',
        l.latitude,
        l.longitude
      ));
    }

    // set map endpoints
    this.pickupCoords = [route.start.latitude, route.start.longitude];
    this.destinationCoords = [route.destination.latitude, route.destination.longitude];

    // treat as already “clicked”
    this.clickCount = 2;

    // quick estimate from straight-line segments (good enough for pricing)
    const stops = locs.map(x => ({ lat: x.latitude, lng: x.longitude }));
    this.setEstimatedDistanceFromStops(stops);
  }

  handleMapClick(event: { lat: number; lng: number }) {
    // If user already used favorite route, start over on click (optional but intuitive)
    if (this.clickCount >= 2 && this.locations.length > 2) {
      // reset to 2 points
      while (this.locations.length) this.locations.removeAt(0);
      this.locations.push(this.newLocationGroup('Pickup selected', event.lat, event.lng));
      this.locations.push(this.newLocationGroup('', 0, 0));
      this.pickupCoords = [event.lat, event.lng];
      this.destinationCoords = undefined;
      this.clickCount = 1;
      return;
    }

    if (this.clickCount === 0) {
      this.pickupCoords = [event.lat, event.lng];
      this.locations.at(0).patchValue({
        address: 'Pickup selected',
        latitude: event.lat,
        longitude: event.lng
      });
      this.clickCount = 1;
      return;
    }

    // destination
    this.destinationCoords = [event.lat, event.lng];
    this.locations.at(this.locations.length - 1).patchValue({
      address: 'Destination selected',
      latitude: event.lat,
      longitude: event.lng
    });
    this.clickCount = 2;

    // update estimate from all current points
    const stops = (this.locations.value as any[])
      .filter(l => l.latitude && l.longitude)
      .map(l => ({ lat: l.latitude, lng: l.longitude }));

    if (stops.length >= 2) this.setEstimatedDistanceFromStops(stops);
  }

  private setEstimatedDistanceFromStops(stops: {lat:number; lng:number}[]) {
    let total = 0;
    for (let i = 0; i < stops.length - 1; i++) {
      total += this.getDistanceKm(stops[i], stops[i + 1]);
    }
    this.rideForm.patchValue({ estimatedDistanceKm: Number(total.toFixed(2)) });
  }

  private getDistanceKm(a: any, b: any): number {
    const R = 6371;
    const dLat = this.toRad(b.lat - a.lat);
    const dLon = this.toRad(b.lng - a.lng);
    const lat1 = this.toRad(a.lat);
    const lat2 = this.toRad(b.lat);
    const aVal =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1) * Math.cos(lat2) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(aVal), Math.sqrt(1 - aVal));
    return R * c;
  }

  private toRad(value: number): number {
    return value * Math.PI / 180;
  }

  submitRide() {
    // require at least first and last to be chosen
    const locs = this.locations.value as any[];
    const start = locs[0];
    const end = locs[locs.length - 1];

    if (!start?.latitude || !start?.longitude || !end?.latitude || !end?.longitude) {
      alert('Please select pickup and destination on the map.');
      return;
    }

    if (this.rideForm.invalid) {
      this.rideForm.markAllAsTouched();
      return;
    }

    this.rideService.createRideRequest(this.rideForm.value).subscribe({
      next: () => alert('Ride request sent successfully!'),
      error: () => alert('Failed to create ride request.')
    });
  }
}