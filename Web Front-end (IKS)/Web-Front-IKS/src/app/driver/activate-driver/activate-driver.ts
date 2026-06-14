import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-activate-driver',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './activate-driver.html',
  styleUrls: ['./activate-driver.css']
})
export class ActivateDriver implements OnInit {

  token = '';
  form!: FormGroup;
  submitting = false;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const t = this.route.snapshot.queryParamMap.get('token');
    this.token = t ?? '';

    this.form = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirm: ['', Validators.required]
    });

    if (!this.token) {
      alert('Missing activation token.');
    }
  }

  submit() {
    if (!this.token) {
      alert('Missing activation token.');
      return;
    }

    if (this.form.invalid) return;

    const { password, confirm } = this.form.value;
    if (password !== confirm) {
      alert('Passwords do not match');
      return;
    }

    this.submitting = true;

    this.authService.activateDriver(this.token, this.form.value.password)
    .subscribe({
      next: () => alert('Account activated'),
      error: (err) => alert(err?.error ?? 'Activation failed')
    });
  }
}