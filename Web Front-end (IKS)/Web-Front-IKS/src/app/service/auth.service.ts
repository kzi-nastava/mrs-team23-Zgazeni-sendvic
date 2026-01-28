import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  activate(token: string, password: string) {
    return this.http.post('/api/auth/activate', { token, password });
  }
}
