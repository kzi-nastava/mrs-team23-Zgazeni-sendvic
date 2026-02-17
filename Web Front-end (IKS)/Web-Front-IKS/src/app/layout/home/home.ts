import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Map as MapComponent, VehiclePosition } from '../../map/map';
import { RouteEstimationPanel } from '../../route-estimation-panel/route-estimation-panel';
import { RouteEstimationService } from '../../service/route.estimation.serivce';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

interface VehiclePositionsResponse {
  vehiclePositions: VehiclePosition[];
}

@Component({
  selector: 'app-home',
  imports: [RouterModule, MapComponent, RouteEstimationPanel, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements AfterViewInit {
  @ViewChild(MapComponent) mapComponent?: MapComponent;
  private readonly geocodeCache = new globalThis.Map<string, [number, number]>();
  private lastRouteKey: string | null = null;

  constructor(
    private http: HttpClient,
    public panelService: RouteEstimationService
  ) {}

  ngOnInit() {
    this.panelService.hidePanel();
  }

  ngAfterViewInit(): void {
    this.loadVehiclePositions();
  }

  onRouteRequest(event: { start: string; end: string }): void {
    const start = event.start?.trim();
    const end = event.end?.trim();

    if (!start || !end) {
      return;
    }

    const routeKey = `${start}|${end}`;
    if (routeKey === this.lastRouteKey) {
      return;
    }

    this.lastRouteKey = routeKey;

    forkJoin({
      start: this.geocodeAddress(start),
      end: this.geocodeAddress(end),
    }).subscribe(({ start: startCoords, end: endCoords }) => {
      if (!startCoords || !endCoords) {
        console.warn('Unable to geocode start or end location for route estimation.');
        return;
      }

      this.mapComponent?.updateRideLocation(startCoords, endCoords);
      this.mapComponent?.fitToBounds([startCoords, endCoords]);
    });
  }

  private loadVehiclePositions(): void {
    this.http
      .get<VehiclePositionsResponse>('http://localhost:8080/api/vehicle-positions')
      .subscribe({
        next: (response) => {
          this.mapComponent?.setVehicleMarkers(response.vehiclePositions);
        },
        error: (err) => {
          console.error('Error fetching vehicle positions:', err);
          console.error('Error status:', err.status);
          console.error('Error message:', err.message);
        },
      });
  }

  private geocodeAddress(address: string) {
    const cached = this.geocodeCache.get(address);
    if (cached) {
      return of(cached);
    }

    const url = `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&q=${encodeURIComponent(address)}`;
    return this.http.get<Array<{ lat: string; lon: string }>>(url).pipe(
      map((results) => {
        const first = results?.[0];
        if (!first) {
          return null;
        }

        const lat = Number(first.lat);
        const lon = Number(first.lon);
        if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
          return null;
        }

        const coords: [number, number] = [lat, lon];
        this.geocodeCache.set(address, coords);
        return coords;
      }),
      catchError(() => of(null))
    );
  }
}
