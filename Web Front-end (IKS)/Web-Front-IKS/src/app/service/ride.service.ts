import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RideRequestDTO } from '../models/ride-request.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RideService {

  private apiUrl = 'http://localhost:8080/api/riderequest';

  constructor(private http: HttpClient) {}

  createRideRequest(dto: RideRequestDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, dto);
  }

  getRideRequestStatus(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }
}
