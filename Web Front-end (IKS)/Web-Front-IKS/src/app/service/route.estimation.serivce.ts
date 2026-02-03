import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RouteEstimationService {
  showEstimationPanel = signal(false);

  constructor(private http: HttpClient) {}

  /**
   * Send route estimation request to backend.
   * Adjust endpoint if your backend uses a different path.
   */
  estimateRoute(body: { beginningDestination: string; endingDestination: string }): Observable<any> {
    return this.http.post<any>('http://localhost:8080/api/ride-estimation', body);
  }

  togglePanel() {
    this.showEstimationPanel.set(!this.showEstimationPanel());
  }

  hidePanel() {
    this.showEstimationPanel.set(false);
  }
}
