import { Component, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../service/auth.service';
import { LoginRequest } from '../../models/auth.models';

@Component({
  selector: 'app-login-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    CommonModule,
    RouterModule,
    
  ],
  templateUrl: './login-form.html',
  styleUrl: './login-form.css',
})
export class LoginForm {
  form!: FormGroup;
  hidePassword = true;
  loginError = signal<string | null>(null);

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      remember: [false],
    });
  }

  togglePasswordVisibility() {
    this.hidePassword = !this.hidePassword;
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    
    this.loginError.set(null);  // Clear any previous errors
    const request: LoginRequest = this.form.value;
    this.authService.login(request).subscribe({
      next: (response: { user: { role: any; }; token: any; }) => {
        console.log('Login successful', response);
        console.log('User role:', response.user?.role ?? '');
        // Token is already stored by auth service
        if (response?.token) {
          this.authService.fetchProfilePicture(response.token).subscribe({
            next: (blob: Blob) => {
              const objectUrl = URL.createObjectURL(blob);
              console.log('Profile picture URL:', objectUrl);
              this.authService.storeProfilePicture(blob);
            },
            error: (error: any) => console.error('Profile picture fetch failed', error)
          });
        }
        this.router.navigate(['/']);
      },
      error: (error: any) => {
        console.error('Login failed', error);
        this.loginError.set('Incorrect email or password');
      }
    });
  }

  getErrorMessage(label: string, controlName: string): string {
    const control = this.form.get(controlName);
    if (!control || !control.errors) return '';

    if (control.errors['required']) return `${label} is required`;
    if (control.errors['email']) return `Please enter a valid ${label.toLowerCase()}`;
    if (control.errors['minlength']) {
      const min = control.errors['minlength'].requiredLength;
      return `${label} must be at least ${min} characters`;
    }
    return 'Invalid input';
  }
}
