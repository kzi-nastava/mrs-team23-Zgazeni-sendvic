import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { DriverService } from '../../service/driver.service';
import { catchError, finalize, of, timeout } from 'rxjs';

type VehicleView = { id: number; model: string; licensePlate: string };

@Component({
  selector: 'app-register-driver',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './register-driver.html',
  styleUrls: ['./register-driver.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterDriver implements OnInit {
  form!: FormGroup;

  vehicles: VehicleView[] = [];
  loadingVehicles = false;
  creating = false;
  errorMsg: string | null = null;

  constructor(
    private fb: FormBuilder,
    private driverService: DriverService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', [Validators.required]],
      surname: ['', [Validators.required]], // UI field; we'll map -> lastName
      phone: ['', [Validators.required]],   // UI field; we'll map -> phoneNumber
      vehicleId: [null, [Validators.required]]
    });

    this.loadVehicles();
  }

  private loadVehicles() {
    this.loadingVehicles = true;
    this.errorMsg = null;
    this.cdr.markForCheck();

    this.driverService.getVehicles().pipe(
      timeout(8000),
      catchError((err) => {
        console.error('Failed to load vehicles', err);
        this.errorMsg = 'Failed to load vehicles.';
        return of([] as VehicleView[]);
      }),
      finalize(() => {
        this.loadingVehicles = false;
        this.cdr.markForCheck();
      })
    ).subscribe((list: any) => {
      this.vehicles = (list ?? []).map((v: any) => ({
        id: Number(v.id),
        model: v.model,
        licensePlate: v.licensePlate ?? v.registration ?? v.plate ?? ''
      }));
      this.cdr.markForCheck();
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.creating = true;
    this.errorMsg = null;
    this.cdr.markForCheck();

    // IMPORTANT: map UI map -> backend DTO field names
    const payload = {
      email: this.form.value.email,
      name: this.form.value.name,
      lastName: this.form.value.surname,
      phoneNumber: this.form.value.phone,
      vehicleId: Number(this.form.value.vehicleId),
      address: null,
      imgString: null
    };

    this.driverService.createDriver(payload).pipe(
      timeout(8000),
      catchError((err) => {
        console.error('Failed to create driver', err);
        this.errorMsg = err?.error ?? 'Failed to create driver.';
        return of(null);
      }),
      finalize(() => {
        this.creating = false;
        this.cdr.markForCheck();
      })
    ).subscribe((res) => {
      if (!res) return;
      alert('Driver created. Activation email sent.');
      this.form.reset();
    });
  }
}