import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { AuthService } from "./auth.service";
import { Observable } from "rxjs";

type VehicleView = { id: number; model: string; licensePlate: string };

@Injectable({ providedIn: 'root' })
export class DriverService {
private readonly apiUrl = 'http://localhost:8080/api/driver';
  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  createDriver(data: any) {
    return this.http.post(this.apiUrl, data);
  }

  createVehicle(data: any) {
    return this.http.post(`${this.apiUrl}/vehicle`, data);
  }

  getVehicles(): Observable<VehicleView[]> {
    return this.http.get<VehicleView[]>(`${this.apiUrl}/vehicles`);
  }

  getRideDecisionInfo(token: string) {
    return this.http.get<any>(`${this.apiUrl}/ride-decision`, {
      params: { token }
    });
  }

  acceptRide(token: string) {
    return this.http.post(`${this.apiUrl}/ride-decision/accept`, { token });
  }

  rejectRide(token: string) {
    return this.http.post(`${this.apiUrl}/ride-decision/reject`, { token });
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
