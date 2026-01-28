import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-activate-driver',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './activate-driver.html'
})
export class ActivateDriver implements OnInit {

  token!: string;
  form!: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token')!;

    this.form = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirm: ['', Validators.required]
    });
  }

  submit() {
    if (this.form.invalid) {
      return;
    }

    if (this.form.value.password !== this.form.value.confirm) {
      alert('Passwords do not match');
      return;
    }

    this.authService.activate(this.token, this.form.value.password!)
      .subscribe(() => alert('Account activated'));
  }
}
