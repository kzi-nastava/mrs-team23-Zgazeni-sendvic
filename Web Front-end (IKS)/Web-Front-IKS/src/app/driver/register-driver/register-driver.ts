import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatFormField, MatLabel } from "@angular/material/form-field";
import { MatOption } from "@angular/material/select";

@Component({
  selector: 'app-register-driver',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormField, MatLabel, MatOption, RouterModule],
  templateUrl: './register-driver.html',
  styleUrls: ['./register-driver.css']
})
export class RegisterDriver implements OnInit {

  form!: FormGroup;

  vehicles = [
    { id: 1, model: 'Toyota Prius', licensePlate: 'BG-123-AA' },
    { id: 2, model: 'Mercedes E-Class', licensePlate: 'NS-987-ZZ' }
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: [''],
      name: [''],
      surname: [''],
      phone: [''],
      vehicleId: ['']
    });
  }

  submit() {
    console.log(this.form.value);
  }
}

