import { Component } from '@angular/core';
import { FormBuilder, FormArray, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RideService } from '../service/ride.service';
import { VehicleType } from '../models/ride-request.model';
import { CommonModule } from '@angular/common';
import { ViewChild } from '@angular/core';
import { Map } from '../map/map';


@Component({
  selector: 'app-ride-order',
  templateUrl: './ride-order.html',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Map]
})
export class RideOrder {

  @ViewChild(Map) mapComponent!: Map;
  pickupCoords?: [number, number];
  destinationCoords?: [number, number];

  private clickCount = 0;

  vehicleTypes = Object.values(VehicleType);
  rideForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private rideService: RideService
  ) {
    this.rideForm = this.fb.group({
      locations: this.fb.array([]),
      vehicleType: [VehicleType.STANDARD, Validators.required],
      babiesAllowed: [false],
      petsAllowed: [false],
      scheduledTime: [null],
      invitedPassengers: [[]],
      estimatedDistanceKm: [0]
    });

    this.addLocation(); // start
    this.addLocation(); // destination
  }

  get locations(): FormArray {
    return this.rideForm.get('locations') as FormArray;
  }

  addLocation() {
    this.locations.push(
      this.fb.group({
        address: ['', Validators.required],
        latitude: [0],
        longitude: [0]
      })
    );
  }

  removeLocation(index: number) {
    if (this.locations.length > 2) {
      this.locations.removeAt(index);
    }
  }

  handleMapClick(event: { lat: number; lng: number }) {

    if (this.clickCount === 0) {
      // FIRST CLICK → PICKUP
      this.pickupCoords = [event.lat, event.lng];

      this.locations.at(0).patchValue({
        address: 'Pickup selected',
        latitude: event.lat,
        longitude: event.lng
      });

      this.clickCount++;

    } else {
      // SECOND CLICK (or later) → DESTINATION
      this.destinationCoords = [event.lat, event.lng];

      this.locations.at(1).patchValue({
        address: 'Destination selected',
        latitude: event.lat,
        longitude: event.lng
      });
    }
  }

  submitRide() {
    if (!this.pickupCoords || !this.destinationCoords) {
      alert('Please select pickup and destination on the map.');
      return;
    }

    if (this.rideForm.invalid) return;

    this.rideService.createRideRequest(this.rideForm.value)
      .subscribe({
        next: () => alert('Ride request sent successfully!'),
        error: () => alert('Failed to create ride request.')
      });
  }
}

