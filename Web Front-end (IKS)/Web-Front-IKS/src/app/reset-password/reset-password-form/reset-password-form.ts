import { Component, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../service/auth.service';
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
export class ResetPasswordForm implements OnInit {
  form!: FormGroup;
  hidePassword = true;
  hideConfirmPassword = true;
  tokenFromUrl: string = '';
  resetMessage = signal<string>('');

  constructor(private fb: FormBuilder, private authService: AuthService, private route: ActivatedRoute, private router: Router) {
    this.form = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        this.tokenFromUrl = token;
        console.log('Token picked up from URL:', this.tokenFromUrl);
      } else {
        console.warn('No token found in URL');
      }
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) return null;
    if (!password.value || !confirmPassword.value) return null;

    if (password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return null;
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

    if (!this.tokenFromUrl) {
      console.error('No token available');
      this.resetMessage.set('No reset token provided');
      return;
    }

    const request: ResetPasswordRequest = {
      token: this.tokenFromUrl,
      newPassword: this.form.value.password,
    };

    console.log('Submitting reset password with token:', this.tokenFromUrl);
    this.authService.resetPassword(request).subscribe({
      next: (response) => {
        console.log('Reset password successful', response);
        this.resetMessage.set('Password reset successfully! ');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (error) => {
        console.error('Reset password failed', error);
        if (error.message === 'success') {
          this.resetMessage.set('Password reset successfully!');
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        } else {
          this.resetMessage.set('Invalid Token');
        }
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
