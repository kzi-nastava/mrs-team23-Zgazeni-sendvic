import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { AccountService } from '../../service/account.service';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { GetAccountDTO } from '../../models/account.dto';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  templateUrl: './profile-edit.html',
  styleUrls: ['./profile-edit.css'],
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule]
})
export class ProfileEdit implements OnInit {

  form!: FormGroup;
  selectedImageBase64?: string;
  message?: string;
  private accountSub?: Subscription;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      lastName: ['', Validators.required],
      address: ['', Validators.required],
      phoneNumber: ['', Validators.required]
    });

    // Subscribe to BehaviorSubject or fetch current account
    this.accountSub = this.accountService.getMyAccount().subscribe({
      next: (acc: GetAccountDTO) => {
        this.form.patchValue({
          name: acc.name,
          lastName: acc.lastName,
          address: acc.address,
          phoneNumber: acc.phoneNumber
        });
        this.selectedImageBase64 = acc.imgString ?? undefined;
      },
      error: (err) => console.error('Failed to load account', err)
    });
  }

  onPhotoSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      this.selectedImageBase64 = (reader.result as string).split(',')[1];
    };
    reader.readAsDataURL(file);
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dto = {
      name: this.form.value.name,
      lastName: this.form.value.lastName,
      address: this.form.value.address,
      phoneNumber: this.form.value.phoneNumber,
      imgString: this.selectedImageBase64
    };

    this.accountService.updateAccount(dto).subscribe({
      next: () => this.router.navigate(['/profile']),
      error: (err) => console.error('Failed to update account', err)
    });
  }

  ngOnDestroy(): void {
    this.accountSub?.unsubscribe();
  }
}

