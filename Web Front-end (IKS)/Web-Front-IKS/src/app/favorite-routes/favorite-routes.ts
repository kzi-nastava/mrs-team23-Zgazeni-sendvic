import { Component, ChangeDetectionStrategy, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { RouteService } from '../service/route.service';
import { RouteDTO } from '../models/route.dto';
import { finalize, catchError, of, timeout } from 'rxjs';

type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page (0-based)
};

@Component({
  selector: 'app-favorite-routes',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatCardModule, MatIconModule],
  templateUrl: './favorite-routes.html',
  styleUrls: ['./favorite-routes.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FavoriteRoutes implements OnInit {

  displayedColumns: string[] = ['route', 'actions'];
  favoriteRoutes: RouteDTO[] = [];
  loading = false;

  // pagination state
  pageIndex = 0;        // 0-based
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  constructor(
    private routeService: RouteService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number) {
    if (page < 0) return;
    if (this.totalPages > 0 && page >= this.totalPages) return;

    this.loading = true;
    this.cdr.markForCheck();

    this.routeService.getFavoritesPaged(page, this.pageSize).pipe(
      timeout(8000),

      catchError((err) => {
        this.favoriteRoutes = [];
        this.totalElements = 0;
        this.totalPages = 0;
        alert('Failed to load favorite routes.');
        // return empty page shape to keep code simple
        const empty: PageResponse<RouteDTO> = {
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: this.pageSize,
          number: page
        };
        return of(empty);
      }),

      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe((res: PageResponse<RouteDTO>) => {
      this.favoriteRoutes = res?.content ?? [];
      this.totalElements = res?.totalElements ?? 0;
      this.totalPages = res?.totalPages ?? 0;
      this.pageIndex = res?.number ?? page;
      this.cdr.markForCheck();
    });
  }

  nextPage() {
    this.loadPage(this.pageIndex + 1);
  }

  prevPage() {
    this.loadPage(this.pageIndex - 1);
  }

  useRoute(route: RouteDTO) {
    this.router.navigate(['/ride-order'], { state: { selectedRoute: route } });
  }

  formatLocation(loc: { latitude: number; longitude: number }): string {
    return `${loc.latitude.toFixed(5)}, ${loc.longitude.toFixed(5)}`;
  }

  formatRoute(route: RouteDTO): string[] {
    const locs = [route.start, ...(route.midPoints ?? []), route.destination];
    return locs.map(l => this.formatLocation(l));
  }
}
