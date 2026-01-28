import { Component, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { FormGroup } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule, Router } from '@angular/router';
import { AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest } from '../../models/auth.models';


@Component({
  selector: 'app-registration-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    CommonModule,
    MatIconModule,
    RouterModule
    
   
  ],
  templateUrl: './registration-form.html',
  styleUrl: './registration-form.css',
})
export class RegistrationForm {
  form!: FormGroup;
  hidePassword = true;  // Toggle password visibility
  hideConfirmPassword = true;
  selectedPhotoFile: File | null = null;
  registerError = signal<string | null>(null);

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required,Validators.pattern(/^\d{10,}$/)]],  // At least 10 digits
      adress: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    }, { validators: this.passwordMatchValidator });
  }

  submit() {
  this.form.markAllAsTouched();   
  if (this.form.invalid) {
    console.log('Form is invalid');
    return;
  }
  
  this.registerError.set(null);  // Clear any previous errors
  
  const request: RegisterRequest = {
    firstName: this.form.value.firstName,
    lastName: this.form.value.lastName,
    email: this.form.value.email,
    phoneNum: this.form.value.phone,
    address: this.form.value.address,
    password: this.form.value.password,
    pictUrl: "DefaultUrl",
  };
      this.authService.register(request).subscribe({
        next: (response) => {
          console.log('Register successful', response);
          this.router.navigate(['/login']);
        },
        error: (error) => {
          // Handle special case where 201 response had parsing error
          if (error.message === 'success') {
            console.log('Register successful (empty response)');
            this.router.navigate(['/login']);
          } else {
            console.error('Register failed', error);
            this.registerError.set('Registration failed. Please try again.');
          }
        }
      });


}

  togglePasswordVisibility() {
    this.hidePassword = !this.hidePassword;
  }

  toggleConfirmPasswordVisibility() {
    this.hideConfirmPassword = !this.hideConfirmPassword;
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

  onPhotoSelected(event: any) {
  const file = event.target.files[0];
  if (file && file.type.startsWith('image/')) {
    this.selectedPhotoFile = file;
    console.log('Photo selected:', file.name);
  }
}
isHoveringUploadBtn = false;
get photoButtonLabel(): string {
  if (this.isHoveringUploadBtn) return 'Upload photo';
  return this.selectedPhotoFile ? `✓ ${this.selectedPhotoFile.name}` : 'Upload photo';
}

  getErrorMessage(fieldName: string, labelName: string): string {
    const control = this.form.get(fieldName);
    if (!control || !control.errors) return '';

    if (control.errors['required']) return `${labelName} is required`;
    if (control.errors['minlength']) return `${labelName} must be at least ${control.errors['minlength'].requiredLength} characters`;
    if (control.errors['email']) return 'Invalid email format';
    if (control.errors['pattern']) return `${labelName} format is invalid`;
    if (control.errors['passwordMismatch']) return 'Passwords do not match';
    return 'Invalid input';
  }
}
