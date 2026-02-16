import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { PanicNotificationDTO, PageResponse } from '../models/panic.models';

export interface PanicNotificationsQuery {
  page?: number;
  size?: number;
  sort?: string;
  fromDate?: string;
  toDate?: string;
}

@Injectable({ providedIn: 'root' })
export class PanicNotificationsService {
  private apiUrl = 'http://localhost:8080/api/panic-notifications';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getPanicNotifications(query: PanicNotificationsQuery): Observable<PageResponse<PanicNotificationDTO>> {
    const authToken = this.authService.getToken();
    const headers = authToken ? new HttpHeaders({ Authorization: `Bearer ${authToken}` }) : undefined;

    let params = new HttpParams();
    if (query.page !== undefined) params = params.set('page', query.page.toString());
    if (query.size !== undefined) params = params.set('size', query.size.toString());
    if (query.sort) params = params.set('sort', query.sort);
    if (query.fromDate) params = params.set('fromDate', query.fromDate);
    if (query.toDate) params = params.set('toDate', query.toDate);

    return this.http.get<PageResponse<PanicNotificationDTO>>(
      `${this.apiUrl}/retrieve-all`,
      { headers, params }
    );
  }
}
