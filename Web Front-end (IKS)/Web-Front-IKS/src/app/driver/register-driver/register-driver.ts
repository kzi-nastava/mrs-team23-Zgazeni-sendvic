import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { DriverService } from '../../service/driver.service';
import { CreateDriverDTO } from '../../models/driver.dto';

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
  styleUrls: ['./register-driver.css']
})
export class RegisterDriver implements OnInit {

  form!: FormGroup;

  vehicles = [
    { id: 1, model: 'Toyota Prius', licensePlate: 'BG-123-AA' },
    { id: 2, model: 'Mercedes E-Class', licensePlate: 'NS-987-ZZ' }
  ];

  constructor(
    private fb: FormBuilder,
    private driverService: DriverService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', [Validators.required]],
      surname: ['', [Validators.required]],
      phone: ['', [Validators.required]],
      vehicleId: [null, [Validators.required]]
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dto: CreateDriverDTO = {
      email: this.form.value.email,
      name: this.form.value.name,
      surname: this.form.value.surname,
      phone: this.form.value.phone,
      vehicleId: Number(this.form.value.vehicleId)
    };

    this.driverService.createDriver(dto).subscribe({
      next: () => {
        console.log('Driver created');
      },
      error: (err: any) => console.error('Failed to create driver', err)
    });
  }
}


