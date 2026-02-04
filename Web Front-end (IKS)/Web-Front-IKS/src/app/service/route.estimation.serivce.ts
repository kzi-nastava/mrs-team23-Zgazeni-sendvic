import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  RouteEstimationRequest,
  RouteEstimationResponse
} from '../models/route.estimation.models';


@Injectable({
  providedIn: 'root'
})
export class RouteEstimationService {
  private apiUrl = 'http://localhost:8080/api/ride-estimation';
  showEstimationPanel = signal(false);
  routePath = signal<number[][] | null>(null); //for storing route path coordinates

  constructor(private http: HttpClient) {}

  /**
   * Send route estimation request to backend.
   * Adjust endpoint if your backend uses a different path.
   */
  estimateRoute(body: { beginningDestination: string; endingDestination: string }): Observable<RouteEstimationResponse> {
    return this.http.post<RouteEstimationResponse>('http://localhost:8080/api/ride-estimation', body)
    .pipe(
      tap(response => console.log('Route estimation response:', response)),
      catchError(this.handleError)
    );
  }

  togglePanel() {
    this.showEstimationPanel.set(!this.showEstimationPanel())
  }

  hidePanel() {
    this.showEstimationPanel.set(false);
  }

  setRoutePath(path: number[][] | null) {
    this.routePath.set(path);
    //console.log('Route path set to:', path);
  }


  private handleError(error: HttpErrorResponse) {
    // Only log actual HTTP errors (4xx, 5xx)
    console.error('HTTP Error:', error.status, error.message);
    return throwError(() => new Error('Something went wrong; please try again later.'));
  }

}
