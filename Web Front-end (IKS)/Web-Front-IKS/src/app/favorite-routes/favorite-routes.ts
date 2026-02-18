import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { RouteService } from '../service/route.service';
import { RouteDTO } from '../models/route.dto';

@Component({
  selector: 'app-favorite-routes',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatCardModule, MatIconModule],
  templateUrl: './favorite-routes.html',
  styleUrls: ['./favorite-routes.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FavoriteRoutes {

  displayedColumns: string[] = ['route', 'actions'];
  favoriteRoutes: RouteDTO[] = [];
  loading = false;

  constructor(private routeService: RouteService, private router: Router) {
    this.load();
  }

  load() {
    this.loading = true;
    this.routeService.getFavorites().subscribe({
      next: (routes) => {
        this.favoriteRoutes = routes ?? [];
        this.loading = false;
      },
      error: () => {
        this.favoriteRoutes = [];
        this.loading = false;
        alert('Failed to load favorite routes.');
      }
    });
  }

  useRoute(route: RouteDTO) {
    // pass route via router state (no need to expose long query params)
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
