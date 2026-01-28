import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DriverService } from '../../service/driver.service';

@Component({
  selector: 'app-register-driver',
  templateUrl: './register-driver.html'
})
export class RegisterDriver {
  form: any;

  constructor(private fb: FormBuilder, private driverService: DriverService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', Validators.required],
      surname: ['', Validators.required],
      phone: ['', Validators.required],

      vehicleModel: ['', Validators.required],
      vehicleType: ['STANDARD', Validators.required],
      licensePlate: ['', Validators.required],
      seats: [4, Validators.required],
      babyTransport: [false],
      petTransport: [false]
    });
  }

  submit() {
    if (this.form.invalid) return;

    this.driverService.createDriver(this.form.value)
      .subscribe(() => alert('Driver created. Activation email sent.'));
  }
}
