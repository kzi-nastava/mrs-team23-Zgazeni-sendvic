import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class RouteEstimationService {
  showEstimationPanel = signal(false);
  private routePathSignal = signal<L.LatLngTuple[] | null>(null);

  routePath = this.routePathSignal.asReadonly(); //for storing route path coordinates
  private readonly noviSadBounds = {
    minLon: 19.70,
    minLat: 45.18,
    maxLon: 19.98,
    maxLat: 45.36,
  };

  constructor(private http: HttpClient) {}

  geocode(query: string): Observable<LatLng | null> {
    const { minLon, minLat, maxLon, maxLat } = this.noviSadBounds;
    const url = `https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(query)}&limit=1&viewbox=${minLon},${maxLat},${maxLon},${minLat}&bounded=1&countrycodes=rs&addressdetails=1`;

    return this.http.get<NominatimSuggestion[]>(url).pipe(
      map((results) => {
        const top = results?.[0];
        if (!top?.lat || !top?.lon) {
          return null;
        }
        return { lat: Number(top.lat), lon: Number(top.lon) };
      }),
      catchError(this.handleError)
    );
  }

  getRoute(points: LatLng[]): Observable<RouteResult> {
    const coords = points
      .map(p => `${p.lon},${p.lat}`)
      .join(';');

    const url =
      `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;

    return this.http.get<OsrmRouteResponse>(url).pipe(
      map(res => {
        const route = res.routes[0];

        const coordinates = route.geometry.coordinates.map(
          ([lng, lat]) => [lat, lng] as [number, number]
        );

        return {
          distanceMeters: route.distance,
          durationSeconds: route.duration,
          coordinates
        };
      })
    );
  }

  getRouteMulti(points: LatLng[]): Observable<RouteResult> {
    const coords = points
      .map(p => `${p.lon},${p.lat}`)
      .join(';');

    const url =
      `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;

    return this.http.get<OsrmRouteResponse>(url).pipe(
      map(res => {
        const route = res.routes[0];

        const path = route.geometry.coordinates
          .map(([lng, lat]) => [lat, lng] as [number, number]);

        return {
          distanceMeters: route.distance,
          durationSeconds: route.duration,
          coordinates: path
        };
      })
    );
  }

  togglePanel() {
    this.showEstimationPanel.set(!this.showEstimationPanel())
  }

  hidePanel() {
    this.showEstimationPanel.set(false);
  }

  setRoutePath(path: L.LatLngTuple[] | null) {
    this.routePathSignal.set(path);
  }


  private handleError(error: HttpErrorResponse) {
    // Only log actual HTTP errors (4xx, 5xx)
    console.error('HTTP Error:', error.status, error.message);
    return throwError(() => new Error('Something went wrong; please try again later.'));
  }

}

interface NominatimSuggestion {
  place_id: number;
  display_name: string;
  lat: string;
  lon: string;
}

interface LatLng {
  lat: number;
  lon: number;
}

interface OsrmRouteResponse {
  routes: Array<{
    distance: number;
    duration: number;
    geometry: {
      coordinates: number[][];
    };
  }>;
}

export interface RouteResult {
  distanceMeters: number;
  durationSeconds: number;
  coordinates: [number, number][];
}
