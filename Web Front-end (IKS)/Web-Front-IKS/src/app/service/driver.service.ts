import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable({ providedIn: 'root' })
export class DriverService {
  constructor(private http: HttpClient) {}

  createDriver(data: any) {
    return this.http.post('/api/admin/drivers', data);
  }

  getRideDecisionInfo(token: string) {
    return this.http.get<any>(`/api/driver/ride-decision`, {
      params: { token }
    });
  }

  acceptRide(token: string) {
    return this.http.post(`/api/driver/ride-decision/accept`, { token });
  }

  rejectRide(token: string) {
    return this.http.post(`/api/driver/ride-decision/reject`, { token });
  }
}
