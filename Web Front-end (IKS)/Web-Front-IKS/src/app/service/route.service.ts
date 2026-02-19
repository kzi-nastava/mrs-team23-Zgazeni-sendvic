import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RouteDTO } from '../models/route.dto';

@Injectable({ providedIn: 'root' })
export class RouteService {
  private readonly api = 'http://localhost:8080/api/routes';

  constructor(private http: HttpClient) {}

  getFavorites(): Observable<RouteDTO[]> {
    return this.http.get<RouteDTO[]>(`${this.api}/favorites`);
  }

  addFavoriteFromRide(rideId: number): Observable<RouteDTO> {
    return this.http.post<RouteDTO>(`${this.api}/favorites/from-ride/${rideId}`, {});
  }

  removeFavorite(routeId: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/favorites/${routeId}`);
  }
}
