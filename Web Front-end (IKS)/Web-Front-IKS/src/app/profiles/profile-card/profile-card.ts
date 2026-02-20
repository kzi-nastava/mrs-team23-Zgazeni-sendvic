import { Component, HostBinding, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgIf } from '@angular/common';
import { AccountService } from '../../service/account.service';
import { GetAccountDTO } from '../../models/account.dto';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-profile-card',
  standalone: true,
  imports: [NgIf, RouterModule],
  templateUrl: './profile-card.html',
  styleUrls: ['./profile-card.css'],
})
export class ProfileCard implements OnInit, OnDestroy {

  displayName = '';
  name = '';
  surname = '';
  email = '';
  phone = '';
  variant: 'admin' | 'driver' | 'user' = 'user';
  adminButton: { label: string } | null = null;
  driverStatistic: { label: string; value: string | number } | null = null;
  profileImage?: string;

  private accountSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private accountService: AccountService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Subscribe to BehaviorSubject to get live account updates
    this.accountSub = this.accountService.account$.subscribe({
      next: acc => {
        if (acc) this.bindAccount(acc);
      },
      error: err => console.error('Failed to load profile', err)
    });

    // Trigger initial fetch (if not already loaded)
    this.accountService.getMyAccount().subscribe({
      error: err => console.error('Failed to fetch profile', err)
    });
  }

  private bindAccount(acc: GetAccountDTO): void {
    this.displayName = `${acc.name} ${acc.lastName}`;
    this.name = acc.name;
    this.surname = acc.lastName;
    this.email = acc.email;
    this.phone = acc.phoneNumber;

    this.profileImage = acc.imgString
      ? `data:image/jpeg;base64,${acc.imgString}`
      : undefined;

    this.adminButton = null;
    this.driverStatistic = null;

    switch (acc.role) {
      case 'ADMIN':
        this.variant = 'admin';
        this.adminButton = { label: 'Ban Accounts' };
        break;

      case 'DRIVER':
        this.variant = 'driver';
        this.driverStatistic = {
          label: 'Total time spent driving (hours)',
          value: acc.totalDrivingHours ?? 0,
        };
        break;

      default:
        this.variant = 'user';
    }
  }

  getRouteLink(): string | null {
    switch (this.variant) {
      case 'admin': return '/hor-admin';
      case 'driver': return '/hor-driver';
      case 'user': return '/hor-user';
      default: return null;
    }
  }

  onEditClick(): void {
    this.router.navigate(['/profile/edit']);
  }

  onAdminButtonClick(): void {
    this.router.navigate(['/admin/ban-account']);
  }

  @HostBinding('class')
  get hostClasses(): string {
    return `profile-card profile-card--${this.variant}`;
  }

  ngOnDestroy(): void {
    this.accountSub?.unsubscribe();
  }
}
