import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { FormGroup } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';


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

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required,Validators.pattern(/^\d{10,}$/)]],  // At least 10 digits
      adress: ['', Validators.required],
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    });
  }

  submit() {
    if (this.form.invalid) {
      console.log('Form is invalid');
      return;
    }
    console.log('Form submitted:', this.form.value);
  }

  togglePasswordVisibility() {
    this.hidePassword = !this.hidePassword;
  }

  toggleConfirmPasswordVisibility() {
    this.hideConfirmPassword = !this.hideConfirmPassword;
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
    return 'Invalid input';
  }
}
