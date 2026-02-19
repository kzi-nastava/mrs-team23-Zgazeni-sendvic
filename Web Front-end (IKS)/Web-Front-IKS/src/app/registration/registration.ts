import { Component } from '@angular/core';
import { RegistrationForm } from './registration-form/registration-form';

@Component({
  selector: 'app-registration',
  imports: [RegistrationForm],
  templateUrl: './registration.html',
  styleUrl: './registration.css',
})
export class Registration {

}
