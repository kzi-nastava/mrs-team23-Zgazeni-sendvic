import { Component, ViewChild, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormArray, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Map, MapClickResult } from '../map/map';
import { RideService } from '../service/ride.service';
import { VehicleType } from '../models/ride-request.model';
import { RouteDTO } from '../models/route.dto';
import { RouteEstimationFacade } from '../service/route-estimation.facade';
import { RouteEstimationService } from '../service/route.estimation.service';

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
  vehicleTypes = Object.values(VehicleType);
  rideForm: FormGroup;
  activeStopIndex: number | null = null;
  routePath: any;

  constructor(
    private fb: FormBuilder,
    private rideService: RideService,
    private router: Router,
    private routeFacade: RouteEstimationFacade,
    private routeService: RouteEstimationService
  ) {
    this.rideForm = this.fb.group({
      locations: this.fb.array([]),
      vehicleType: [VehicleType.STANDARD],
      babiesAllowed: [false],
      petsAllowed: [false],
      scheduledTime: [null],
      invitedPassengerEmails: [[]],
      estimatedDistanceKm: [0]
    });

    this.routePath = this.routeService.routePath;
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

  private get start() {
    return this.locations.at(0)?.value;
  }

  private get end() {
    return this.locations.at(this.locations.length - 1)?.value;
  }

  get locations(): FormArray {
    return this.rideForm.get('locations') as FormArray;
  }

  private newLocationGroup(address = '', lat = 0, lng = 0) {
    return this.fb.group({
      address: [address],
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
      this.updateRouteEstimate();
    }
  }

  private applyFavoriteRoute(route: RouteDTO) {
    const locs = [route.start, ...(route.midPoints ?? []), route.destination];

    while (this.locations.length) this.locations.removeAt(0);

    locs.forEach((l, idx) => {
      const label =
        idx === 0 ? 'Favorite pickup' :
        idx === locs.length - 1 ? 'Favorite destination' :
        `Favorite stop ${idx}`;

      this.locations.push(this.newLocationGroup(
        label,
        l.latitude,
        l.longitude
      ));
    });

    this.pickupCoords = [route.start.latitude, route.start.longitude];
    this.destinationCoords = [route.destination.latitude, route.destination.longitude];

    const stops = locs.map(x => ({ lat: x.latitude, lng: x.longitude }));
    this.updateRouteEstimate();
  }

  selectLocationOnMap(index: number) {
    this.activeStopIndex = index;
  }

  onMapClick(e: { lat: number; lng: number }) {
    if (this.activeStopIndex === null) return;

    const control = this.locations.at(this.activeStopIndex);

    control.patchValue({
      latitude: e.lat,
      longitude: e.lng,
      address: 'Selected location'
    });

    this.updateRouteEstimate();
  }

  updateRouteEstimate() {
    const locs = this.locations.value as any[];

    const points = locs
      .filter(l => this.isValidCoord(l))
      .map(l => ({
        lat: l.latitude,
        lon: l.longitude
      }));

    if (points.length < 2) return;

    this.routeFacade.estimateRouteByCoords(points)
      .subscribe(route => {
        this.rideForm.patchValue({
          estimatedDistanceKm: route.distanceMeters / 1000
        });

        this.routeService.setRoutePath(route.coordinates);
      });
  }

  private toRad(value: number): number {
    return value * Math.PI / 180;
  }

  submitRide() {
    const locs = this.locations.value as any[];

    const start = locs[0];
    const end = locs[locs.length - 1];

    if (!this.isValidCoord(start) || !this.isValidCoord(end)) {
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

  private isValidCoord(c: any): boolean {
    return (
      c &&
      typeof c.latitude === 'number' &&
      typeof c.longitude === 'number' &&
      !(c.latitude === 0 && c.longitude === 0)
    );
  }
}
