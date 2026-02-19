import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
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
import { MatDialog } from '@angular/material/dialog';
import { HorService } from '../service/hor.service';
import { ARideRequestedUserDTO, URideDetailsRequestedDTO } from '../models/hor.models';
import { DetailedHorUser } from './detailed-hor-user/detailed-hor-user';
import { RouteService } from '../service/route.service';
import { RouteDTO } from '../models/route.dto';

@Component({
  selector: 'app-hor-user',
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
  templateUrl: './hor-user.html',
  styleUrl: './hor-user.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HORUser {
  displayedColumns: string[] = [
    'creationTime',
    'route',
    'beginning',
    'ending',
    'favorite',
    'details'
  ];


  rides: ARideRequestedUserDTO[] = [];
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
    rideID: 'id',
    creationTime: 'creationDate',
    route: 'startLatitude',
    beginning: 'startTime',
    ending: 'endTime'
  };

  private readonly geocodeCache = new Map<string, string>();
  private readonly geocodePending = new Set<string>();

  constructor(
    private horService: HorService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private dialog: MatDialog,
    private routeService: RouteService
  ) {
    this.fetchRides();
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.fetchRides(true);
  }

  clearFilters(): void {
    this.filterForm.reset({ fromDate: null, toDate: null });
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

  formatRoute(ride: ARideRequestedUserDTO): string[] {
    const locations = ride.destinations ?? [];
    return locations.map(loc => this.getLocationLabel(loc));
  }

  onDetailsClick(ride: ARideRequestedUserDTO): void {
    if (!ride.rideID) return;
    
    this.horService.getUserRideDetails(ride.rideID).subscribe({
      next: (details: URideDetailsRequestedDTO) => {
        console.log('Ride Details:', details);
        // Add route information from the ride data
        const detailsWithRoute: URideDetailsRequestedDTO & {
          arrivingPoint?: { latitude: number; longitude: number };
          endingPoint?: { latitude: number; longitude: number };
          destinations?: { latitude: number; longitude: number }[];
        } = {
          ...details,
          arrivingPoint: ride.destinations?.[0],
          endingPoint: ride.destinations?.[ride.destinations.length - 1],
          destinations: ride.destinations?.slice(1, -1)
        };
        this.openDetailedView(detailsWithRoute);
      },
      error: (error) => {
        console.error('Failed to load ride details:', error);
      }
    });
  }

  private openDetailedView(details: URideDetailsRequestedDTO & {
    arrivingPoint?: { latitude: number; longitude: number };
    endingPoint?: { latitude: number; longitude: number };
    destinations?: { latitude: number; longitude: number }[];
  }): void {
    this.dialog.open(DetailedHorUser, {
      data: details,
      width: '90%',
      maxWidth: '1000px',
      maxHeight: '90vh',
      panelClass: 'detailed-ride-dialog'
    });
  }

  private fetchRides(applyFilter = false): void {
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

    this.horService.getUserRides({
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
          this.rides = content;
          this.refreshFavoritesIndexAndMarkRides();
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

  private getLocationLabel(loc: { latitude?: number; longitude?: number }): string {
    const lat = loc.latitude;
    const lon = loc.longitude;
    if (lat === undefined || lon === undefined) return 'Unknown';

    const key = `${lat.toFixed(5)}, ${lon.toFixed(5)}`;
    const cached = this.geocodeCache.get(key);
    if (cached) return cached;

    if (this.geocodePending.has(key)) return key;
    this.geocodePending.add(key);

    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lon}`;
    this.http.get<{ display_name?: string }>(url).subscribe({
      next: response => {
        const city = response.display_name ? response.display_name.split(',')[4]?.trim() : null;
        const name = response.display_name
          ? response.display_name.split(',').slice(0, 2).concat(city ? [city] : []).join(',')
          : key;
        this.geocodeCache.set(key, name);
        this.geocodePending.delete(key);
        this.cdr.markForCheck();
      },
      error: () => {
        this.geocodeCache.set(key, key);
        this.geocodePending.delete(key);
        this.cdr.markForCheck();
      }
    });

    return key;
  }

  private favoritesIndex = new Map<string, number>(); // key = "lat,lng|lat,lng" => routeId

  private buildKeyFromRide(ride: ARideRequestedUserDTO): string | null {
    const locs = ride.destinations ?? [];
    if (locs.length < 2) return null;
    const start = locs[0];
    const end = locs[locs.length - 1];
    if (start.latitude == null || start.longitude == null || end.latitude == null || end.longitude == null) return null;
    return `${start.latitude.toFixed(6)},${start.longitude.toFixed(6)}|${end.latitude.toFixed(6)},${end.longitude.toFixed(6)}`;
  }

  private buildKeyFromRoute(r: RouteDTO): string {
    return `${r.start.latitude.toFixed(6)},${r.start.longitude.toFixed(6)}|${r.destination.latitude.toFixed(6)},${r.destination.longitude.toFixed(6)}`;
  }

  private refreshFavoritesIndexAndMarkRides(): void {
    // Load favorites and mark rides that match (start+end match is “good enough” for MVP)
    this.routeService.getFavorites().subscribe({
      next: (favorites) => {
        this.favoritesIndex.clear();
        for (const f of favorites) {
          this.favoritesIndex.set(this.buildKeyFromRoute(f), f.id);
        }

        for (const r of this.rides as any[]) {
          const k = this.buildKeyFromRide(r);
          r._favoriteRouteId = (k && this.favoritesIndex.get(k)) ? this.favoritesIndex.get(k) : null;
        }

        this.cdr.markForCheck();
      },
      error: () => {
        // ignore, keep UI usable
      }
    });
  }

  toggleFavorite(ride: any): void {
    if (!ride?.rideID) return;

    // if already favorited => remove
    if (ride._favoriteRouteId) {
      const routeId = ride._favoriteRouteId as number;
      this.routeService.removeFavorite(routeId).subscribe({
        next: () => {
          ride._favoriteRouteId = null;
          this.cdr.markForCheck();
        },
        error: () => alert('Failed to remove favorite.')
      });
      return;
    }

    // else add from ride
    this.routeService.addFavoriteFromRide(ride.rideID).subscribe({
      next: (created) => {
        ride._favoriteRouteId = created.id;
        this.cdr.markForCheck();
      },
      error: () => alert('Failed to add favorite.')
    });
  }
}
