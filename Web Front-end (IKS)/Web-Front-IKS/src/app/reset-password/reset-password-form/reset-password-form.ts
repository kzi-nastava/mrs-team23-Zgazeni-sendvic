import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ResetPasswordRequest } from '../../models/auth.models';

@Component({
  selector: 'app-reset-password-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
    CommonModule
  ],
  templateUrl: './reset-password-form.html',
  styleUrl: './reset-password-form.css',
})
export class ResetPasswordForm {
  form!: FormGroup;
  hidePassword = true;
  hideConfirmPassword = true;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.form = this.fb.group({
      token: ['', Validators.required], // Assuming token is provided, e.g., from URL
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) return null;
    if (confirmPassword.errors && !confirmPassword.errors['passwordMismatch']) return null;

    if (password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    } else {
      confirmPassword.setErrors(null);
      return null;
    }
  }

  togglePasswordVisibility() {
    this.hidePassword = !this.hidePassword;
  }

  toggleConfirmPasswordVisibility() {
    this.hideConfirmPassword = !this.hideConfirmPassword;
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { confirmPassword, ...requestData } = this.form.value;
    const request: ResetPasswordRequest = requestData;
    this.authService.resetPassword(request).subscribe({
      next: (response) => {
        console.log('Reset password successful', response);
        // Handle success
      },
      error: (error) => {
        console.error('Reset password failed', error);
        // Handle error
      }
    });
  }

  getErrorMessage(label: string, controlName: string): string {
    const control = this.form.get(controlName);
    if (!control || !control.errors) return '';

    if (control.errors['required']) return `${label} is required`;
    if (control.errors['minlength']) {
      const min = control.errors['minlength'].requiredLength;
      return `${label} must be at least ${min} characters`;
    }
    if (control.errors['passwordMismatch']) return 'Passwords do not match';
    return 'Invalid input';
  }
}
