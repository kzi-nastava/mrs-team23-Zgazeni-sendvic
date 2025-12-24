import { Component, HostBinding, Input, Output, EventEmitter } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-profile-card',
  standalone: true,
  imports: [NgIf, RouterModule],
  templateUrl: './profile-card.html',
  styleUrls: ['./profile-card.css'],
})

export class ProfileCard {
  getRouteLink() {
    if (this.variant === 'admin') return './hor-admin';
    if (this.variant === 'user') return './hor-user';
    if (this.variant === 'driver') return './hor-driver';
    return null;
  }
  @Input() displayName = '';
  @Input() name = '';
  @Input() surname = '';
  @Input() dob = '';
  @Input() email = '';
  @Input() phone = '';
  @Input() password = '';

  @Input() variant: 'admin' | 'driver' | 'user' = 'user';
  @Input() adminButton: { label: string; action?: string } | null = null;
  @Input() driverStatistic: { label: string; value: string | number } | null = null;

  @Output() adminButtonClick = new EventEmitter<void>();

  @HostBinding('class') get hostClasses() {
    return `profile-card profile-card--${this.variant}`;
  }

  onAdminButtonClick() {
    this.adminButtonClick.emit();
  }
}
