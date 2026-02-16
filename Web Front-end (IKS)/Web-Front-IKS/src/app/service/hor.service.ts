import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ARideRequestedDTO, PageResponse, ARideDetailsRequestedDTO, ARideRequestedUserDTO } from '../models/hor.models';

export interface HorAdminQuery {
  page?: number;
  size?: number;
  sort?: string;
  fromDate?: string;
  toDate?: string;
}

@Injectable({ providedIn: 'root' })
export class HorService {
  private apiUrl = 'http://localhost:8080/api/HOR';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getAdminRides(targetId: number, query: HorAdminQuery = {}): Observable<PageResponse<ARideRequestedDTO>> {
    const authToken = this.authService.getToken();
    const headers = authToken ? new HttpHeaders({ Authorization: `Bearer ${authToken}` }) : undefined;

    let params = new HttpParams();
    if (query.page !== undefined) params = params.set('page', query.page);
    if (query.size !== undefined) params = params.set('size', query.size);
    if (query.sort) params = params.set('sort', query.sort);
    if (query.fromDate) params = params.set('fromDate', query.fromDate);
    if (query.toDate) params = params.set('toDate', query.toDate);

    return this.http.get<PageResponse<ARideRequestedDTO>>(
      `${this.apiUrl}/admin/${targetId}`,
      { headers, params }
    );
  }

  getAdminRideDetails(rideId: number): Observable<ARideDetailsRequestedDTO> {
    const authToken = this.authService.getToken();
    const headers = authToken ? new HttpHeaders({ Authorization: `Bearer ${authToken}` }) : undefined;

    return this.http.get<ARideDetailsRequestedDTO>(
      `${this.apiUrl}/admin/detailed/${rideId}`,
      { headers }
    );
  }

  getUserRides(query: HorAdminQuery = {}): Observable<PageResponse<ARideRequestedUserDTO>> {
    const authToken = this.authService.getToken();
    const headers = authToken ? new HttpHeaders({ Authorization: `Bearer ${authToken}` }) : undefined;

    let params = new HttpParams();
    if (query.page !== undefined) params = params.set('page', query.page);
    if (query.size !== undefined) params = params.set('size', query.size);
    if (query.sort) params = params.set('sort', query.sort);
    if (query.fromDate) params = params.set('fromDate', query.fromDate);
    if (query.toDate) params = params.set('toDate', query.toDate);

    return this.http.get<PageResponse<ARideRequestedUserDTO>>(
      `${this.apiUrl}/user`,
      { headers, params }
    );
  }
}
