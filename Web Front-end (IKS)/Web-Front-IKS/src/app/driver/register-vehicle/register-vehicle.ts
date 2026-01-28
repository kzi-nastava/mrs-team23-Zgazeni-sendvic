import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { form } from '@angular/forms/signals';

@Component({
  selector: 'app-register-vehicle',
  templateUrl: './register-vehicle.html',
  styleUrls: ['./register-vehicle.css'],
  imports: [ReactiveFormsModule, RouterModule]
})
export class RegisterVehicle {
  
  form!: FormGroup;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
    model: [''],
    vehicleType: ['STANDARD'],
    licensePlate: [''],
    seats: [4],
    babyTransport: [false],
    petTransport: [false]
  });
  }

  submit() {
    console.log(this.form.value);
  }
}
