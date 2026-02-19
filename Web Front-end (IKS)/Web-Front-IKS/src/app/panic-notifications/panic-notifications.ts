import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PanicNotificationsService } from '../service/panic-notifications.service';
import { PanicNotificationDTO } from '../models/panic.models';
import { AuthService } from '../service/auth.service';

@Component({
  selector: 'app-panic-notifications',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  templateUrl: './panic-notifications.html',
  styleUrl: './panic-notifications.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PanicNotifications implements OnInit, OnDestroy {
  displayedColumns: string[] = [
    'id',
    'callerId',
    'callerName',
    'rideId',
    'createdAt',
    'resolved',
    'resolvedAt',
    'actions'
  ];

  panics: PanicNotificationDTO[] = [];
  totalElements = 0;
  pageIndex = 0;
  pageSize = 10;
  loading = false;
  error = '';

  resolvingIds = new Set<number>();
  resolveErrors = new Map<number, string>();

  currentSort: Sort = { active: '', direction: '' };

  private panicSubscription?: Subscription;
  private resolvedSubscription?: Subscription;
  private connectionSubscription?: Subscription;

  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  filterForm = this.fb.group({
    fromDate: [null as Date | null],
    toDate: [null as Date | null]
  });

  private sortFieldMap: Record<string, string | null> = {
    id: 'id',
    callerId: 'caller',
    callerName: 'caller',
    rideId: 'ride',
    createdAt: 'createdAt',
    resolved: 'resolved',
    resolvedAt: 'resolvedAt'
  };

  constructor(
    private panicService: PanicNotificationsService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Just load the panic notifications for the table
    const userRole = this.authService.getRole();
    if (userRole === 'ADMIN') {
      this.setupSubscriptions();
      this.fetchPanics();
    }
  }

  ngOnDestroy(): void {
    // Clean up subscriptions but do NOT disconnect WebSocket
    // WebSocket stays connected until logout
    this.panicSubscription?.unsubscribe();
    this.resolvedSubscription?.unsubscribe();
    this.connectionSubscription?.unsubscribe();
  }

  private setupSubscriptions(): void {
    // Subscribe to new panic notifications to update the table
    this.panicSubscription = this.panicService.getPanicNotificationsStream().subscribe({
      next: (panic) => {
        // Refresh the table to show the new panic
        this.fetchPanics();
      },
      error: (err) => {
        console.error('Error receiving panic notification:', err);
      }
    });

    // Subscribe to panic resolved notifications to update the table
    this.resolvedSubscription = this.panicService.getPanicResolvedNotificationsStream().subscribe({
      next: (panic) => {
        // Refresh the table to update the status
        this.fetchPanics();
      },
      error: (err) => {
        console.error('Error receiving panic resolved notification:', err);
      }
    });

    // Subscribe to connection status
    this.connectionSubscription = this.panicService.getConnectionStatus().subscribe({
      next: (connected) => {
        if (connected) {
          console.log('WebSocket connected and receiving panic notifications');
        } else {
          console.log('WebSocket disconnected. Attempting to reconnect...');
        }
        this.cdr.markForCheck();
      }
    });
  }



  applyFilters(): void {
    this.pageIndex = 0;
    this.fetchPanics();
  }

  clearFilters(): void {
    this.filterForm.reset({ fromDate: null, toDate: null });
    this.pageIndex = 0;
    this.panics = [];
    this.totalElements = 0;
    this.error = '';
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.fetchPanics();
  }

  onSortChange(sort: Sort): void {
    this.currentSort = sort;
    this.pageIndex = 0;
    this.fetchPanics();
  }

  formatDateTime(dateTime: string): string {
    if (!dateTime) return '-';
    try {
      return new Date(dateTime).toLocaleString();
    } catch {
      return dateTime;
    }
  }

  formatResolved(resolved: boolean): string {
    return resolved ? 'Yes' : 'No';
  }

  resolvePanic(panic: PanicNotificationDTO): void {
    if (!panic.id) return;
    if (panic.resolved) return;

    this.resolvingIds.add(panic.id);
    this.resolveErrors.delete(panic.id);
    this.cdr.markForCheck();

    this.panicService.resolvePanic(panic.id).subscribe({
      next: (updated: PanicNotificationDTO) => {
        const index = this.panics.findIndex(p => p.id === panic.id);
        if (index !== -1) {
          this.panics = [
            ...this.panics.slice(0, index),
            updated,
            ...this.panics.slice(index + 1)
          ];
        }
        this.resolvingIds.delete(panic.id);
        this.cdr.markForCheck();
      },
      error: () => {
        this.resolveErrors.set(panic.id, 'Failed to resolve notification');
        this.resolvingIds.delete(panic.id);
        this.cdr.markForCheck();
      }
    });
  }

  private fetchPanics(): void {
    const fromDate = this.filterForm.value.fromDate 
      ? this.toLocalDateString(this.filterForm.value.fromDate) 
      : null;
    const toDate = this.filterForm.value.toDate 
      ? this.toLocalDateString(this.filterForm.value.toDate) 
      : null;
    const mappedSort = this.sortFieldMap[this.currentSort.active] ?? null;
    const sortParam = this.currentSort.direction && mappedSort
      ? `${mappedSort},${this.currentSort.direction}`
      : undefined;

    this.loading = true;
    this.error = '';

    this.panicService.getPanicNotifications({
      page: this.pageIndex,
      size: this.pageSize,
      sort: sortParam,
      fromDate: fromDate ?? undefined,
      toDate: toDate ?? undefined
    })
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: page => {
          this.panics = page.content ?? [];
          this.totalElements = page.totalElements ?? 0;
          this.pageIndex = page.number ?? this.pageIndex;
          this.pageSize = page.size ?? this.pageSize;
          this.cdr.markForCheck();
        },
        error: () => {
          this.error = 'Failed to load panic notifications. Please try again.';
          this.cdr.markForCheck();
        }
      });
  }

  private toLocalDateString(date: Date): string {
    // Convert to ISO datetime string (YYYY-MM-DDTHH:mm:ss) for LocalDateTime parameter
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }
}
