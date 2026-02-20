import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { AuthService } from "./auth.service";

@Injectable({ providedIn: 'root' })
export class DriverService {
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  createDriver(data: any) {
    return this.http.post('/api/driver', data);
  }

  createVehicle(data: any) {
    return this.http.post('/api/driver/vehicle', data);
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

  requestDriverDeactivation(statusOfDriver: boolean) {
    const token = this.authService.getToken();
    const headers = token
      ? new HttpHeaders({ Authorization: `Bearer ${token}` })
      : undefined;

    return this.http.put(
      `http://localhost:8080/api/driver/deactivate`,
      statusOfDriver,
      { headers, responseType: 'text' }
    );
  }
}
