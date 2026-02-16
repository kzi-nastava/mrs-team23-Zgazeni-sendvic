import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
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
import { PanicNotificationsService } from '../service/panic-notifications.service';
import { PanicNotificationDTO } from '../models/panic.models';

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
    MatTooltipModule
  ],
  templateUrl: './panic-notifications.html',
  styleUrl: './panic-notifications.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PanicNotifications {
  displayedColumns: string[] = [
    'id',
    'callerId',
    'callerName',
    'rideId',
    'createdAt',
    'resolved',
    'resolvedAt'
  ];

  panics: PanicNotificationDTO[] = [];
  totalElements = 0;
  pageIndex = 0;
  pageSize = 10;
  loading = false;
  error = '';

  currentSort: Sort = { active: '', direction: '' };

  private fb = inject(FormBuilder);

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
    private cdr: ChangeDetectorRef
  ) {}

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
