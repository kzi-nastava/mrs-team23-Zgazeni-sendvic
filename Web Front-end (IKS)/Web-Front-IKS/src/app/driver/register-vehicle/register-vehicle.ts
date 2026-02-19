import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';

import { DriverService } from '../../service/driver.service';

type VehicleType = 'STANDARD' | 'LUXURY' | 'VAN';

@Component({
  selector: 'app-register-vehicle',
  standalone: true,
  templateUrl: './register-vehicle.html',
  styleUrls: ['./register-vehicle.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule
  ]
})
export class RegisterVehicle {

  form: FormGroup;
  message: string | null = null;
  error: string | null = null;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private driverService: DriverService,
    private router: Router
  ) {
    this.form = this.fb.group({
      model: ['', [Validators.required, Validators.minLength(2)]],
      vehicleType: ['STANDARD' as VehicleType, Validators.required],
      licensePlate: ['', [Validators.required, Validators.minLength(3)]],
      seats: [4, [Validators.required, Validators.min(1), Validators.max(12)]],
      babyTransport: [false],
      petTransport: [false]
    });
  }

  submit(): void {
    this.message = null;
    this.error = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // Map frontend form -> backend RegisterVehicleDTO
    const payload = {
      model: this.form.value.model,
      registration: this.form.value.licensePlate,     // form field name -> DTO field
      type: this.form.value.vehicleType,              // "STANDARD" | "LUXURY" | "VAN"
      numOfSeats: this.form.value.seats,              // form field name -> DTO field
      babiesAllowed: this.form.value.babyTransport,   // form field name -> DTO field
      petsAllowed: this.form.value.petTransport       // form field name -> DTO field
    };


    this.loading = true;

    this.driverService.createVehicle(payload).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.message = 'Vehicle registered successfully.';

        // Optional: go back and pass created vehicle id (works if you use query params in RegisterDriver)
        const newVehicleId = res?.id;
        this.router.navigate(['/register-driver'], {
          queryParams: newVehicleId ? { vehicleId: newVehicleId } : undefined
        });
      },
      error: (err: { error: { message: any; }; }) => {
        this.loading = false;
        // your backend returns plain string on error; handle both cases
        this.error = err?.error?.message ?? err?.error ?? 'Failed to register vehicle.';
        console.error(err);
      }
    });
  }
}

