import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ForgotPasswordRequest } from '../../models/auth.models';

@Component({
  selector: 'app-forgot-password-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    RouterModule,
    CommonModule
  ],
  templateUrl: './forgot-password-form.html',
  styleUrl: './forgot-password-form.css',
})
export class ForgotPasswordForm {
  form!: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request: ForgotPasswordRequest = this.form.value;
    this.authService.forgotPassword(request).subscribe({
      next: (response) => {
        console.log('Forgot password request successful', response);
        // Handle success
      },
      error: (error) => {
        console.error('Forgot password failed', error);
        // Handle error
      }
    });
  }

  getErrorMessage(label: string, controlName: string): string {
    const control = this.form.get(controlName);
    if (!control || !control.errors) return '';

    if (control.errors['required']) return `${label} is required`;
    if (control.errors['email']) return `Please enter a valid ${label.toLowerCase()}`;
    return 'Invalid input';
  }
}
