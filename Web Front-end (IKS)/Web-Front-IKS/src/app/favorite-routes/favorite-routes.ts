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

  constructor(
    private routeService: RouteService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.loading = true;
    this.cdr.markForCheck();

    this.routeService.getFavorites().pipe(
      timeout(8000),

      catchError((err) => {
        this.favoriteRoutes = [];
        // keep your alert if you want
        alert('Failed to load favorite routes.');
        return of([] as RouteDTO[]);
      }),

      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe((routes) => {
      this.favoriteRoutes = routes ?? [];
      this.cdr.markForCheck();
    });
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
