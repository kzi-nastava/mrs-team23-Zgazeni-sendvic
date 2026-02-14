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
import { HorService } from '../service/hor.service';
import { ARideRequestedDTO, RideStatus } from '../models/hor.models';

@Component({
  selector: 'app-hor-admin',
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
    MatProgressSpinnerModule
  ],
  templateUrl: './hor-admin.html',
  styleUrl: './hor-admin.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HORAdmin {
  displayedColumns: string[] = [
    'rideID',
    'creationTime',
    'route',
    'beginning',
    'ending',
    'start',
    'end',
    'status',
    'canceledBy',
    'price',
    'panic'
  ];

  rides: ARideRequestedDTO[] = [];
  totalElements = 0;
  pageIndex = 0;
  pageSize = 10;
  loading = false;
  error = '';

  currentSort: Sort = { active: '', direction: '' };

  private fb = inject(FormBuilder);

  filterForm = this.fb.group({
    targetId: [null as number | null],
    fromDate: [null as Date | null],
    toDate: [null as Date | null]
  });

  private sortFieldMap: Record<string, string | null> = {
    rideID: 'id',
    creationTime: 'creationDate',
    route: 'locations',
    beginning: 'startTime',
    ending: 'endTime',
    start: 'locations',
    end: 'locations',
    status: 'status',
    canceledBy: 'canceler',
    price: 'price',
    panic: 'panic'
  };

  constructor(private horService: HorService, private cdr: ChangeDetectorRef) {}

  applyFilters(): void {
    this.pageIndex = 0;
    this.fetchRides(true);
  }

  clearFilters(): void {
    this.filterForm.reset({ targetId: null, fromDate: null, toDate: null });
    this.pageIndex = 0;
    this.rides = [];
    this.totalElements = 0;
    this.error = '';
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.fetchRides();
  }

  onSortChange(sort: Sort): void {
    this.currentSort = sort;
    this.pageIndex = 0;
    this.fetchRides();
  }

  formatRoute(ride: ARideRequestedDTO): string[] {
    const segments = [ride.arrivingPoint, ...(ride.destinations ?? []), ride.endingPoint].filter(Boolean);
    const uniqueSegments = segments.filter((loc, index, array) => {
      if (index === 0) return true;
      const prev = array[index - 1];
      return !(loc.latitude === prev.latitude && loc.longitude === prev.longitude);
    });

    return uniqueSegments.map(loc => {
      const lat = loc.latitude;
      const lon = loc.longitude;
      if (lat === undefined || lon === undefined) return 'Unknown';
      return `${lat.toFixed(5)}, ${lon.toFixed(5)}`;
    });
  }

  canceledLabel(ride: ARideRequestedDTO): string {
    return ride.whoCancelled ? 'Yes' : 'No';
  }

  canceledByLabel(ride: ARideRequestedDTO): string {
    return ride.whoCancelled ? String(ride.whoCancelled) : '-';
  }

  formatStatus(status: RideStatus): string {
    switch (status) {
      case RideStatus.SCHEDULED:
        return 'Scheduled';
      case RideStatus.ACTIVE:
        return 'Active';
      case RideStatus.FINISHED:
        return 'Finished';
      case RideStatus.CANCELED:
        return 'Canceled';
      default:
        return String(status ?? '');
    }
  }

  private fetchRides(requireTarget = false): void {
    const targetId = this.filterForm.value.targetId;
    if (!targetId) {
      if (requireTarget) {
        this.error = 'Target ID is required.';
      }
      return;
    }

    const fromDate = this.formatDateParam(this.filterForm.value.fromDate ?? null);
    const toDate = this.formatDateParam(this.filterForm.value.toDate ?? null);
    const mappedSort = this.sortFieldMap[this.currentSort.active] ?? null;
    const sortParam = this.currentSort.direction && mappedSort
      ? `${mappedSort},${this.currentSort.direction}`
      : undefined;

    this.loading = true;
    this.error = '';

    this.horService.getAdminRides(targetId, {
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
          const content = page.content ?? [];
          this.rides = content.map(ride => {
            const status = (ride as unknown as { Status?: ARideRequestedDTO['status'] }).Status;
            return status ? { ...ride, status } : ride;
          });
          this.totalElements = page.totalElements ?? 0;
          this.pageIndex = page.number ?? this.pageIndex;
          this.pageSize = page.size ?? this.pageSize;
          this.cdr.markForCheck();
        },
        error: () => {
          this.error = 'Failed to load rides. Please try again.';
          this.cdr.markForCheck();
        }
      });
  }

  private formatDateParam(date: Date | null): string | null {
    if (!date) return null;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

}
