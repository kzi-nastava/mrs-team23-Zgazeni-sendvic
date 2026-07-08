import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { RideReportDTO } from '../models/ride-report.model';

@Injectable({ providedIn: 'root' })
export class RideReportService {

  private baseUrl = 'http://localhost:8080/api/HOR';

  constructor(private http: HttpClient) {}

  getReport(from?: string, to?: string, targetUserId?: number): Observable<RideReportDTO> {

    let params = new HttpParams();

    if (from) {
      params = params.set('from', from);
    }

    if (to) {
      params = params.set('to', to);
    }

    if(targetUserId){
      params=params.set(
        "targetUserId",
        targetUserId
      );
    }

    return this.http.get<RideReportDTO>(
      `${this.baseUrl}/report`,
      { params }
    );
  }
}