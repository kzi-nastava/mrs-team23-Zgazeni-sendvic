import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

interface Location {
  latitude: number;
  longitude: number;
}

interface FavoriteRoute {
  id: number;
  locations: Location[];
}

@Component({
  selector: 'app-favorite-routes',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './favorite-routes.html',
  styleUrls: ['./favorite-routes.css']
})
export class FavoriteRoutes {

  displayedColumns: string[] = ['route', 'actions'];

  favoriteRoutes: FavoriteRoute[] = [
    {
      id: 1,
      locations: [
        { latitude: 44.7866, longitude: 20.4489 },
        { latitude: 44.8125, longitude: 20.4612 },
        { latitude: 44.8176, longitude: 20.4569 }
      ]
    },
    {
      id: 2,
      locations: [
        { latitude: 44.7701, longitude: 20.4750 },
        { latitude: 44.7812, longitude: 20.4689 }
      ]
    }
  ];

  useRoute(route: FavoriteRoute) {
    // later: auto-fill ride order with route.locations
    console.log('Selected route:', route);
  }

  formatLocation(loc: Location): string {
    return `${loc.latitude.toFixed(5)}, ${loc.longitude.toFixed(5)}`;
  }
}
