import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  ForgotPasswordRequest,
  ForgotPasswordResponse,
  ResetPasswordRequest,
  ResetPasswordResponse
} from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'jwt_token';
  private userIdKey = 'user_id';

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          console.log('Login response:', response);
          if (response.token) {
            localStorage.setItem(this.tokenKey, response.token);
          }
          if (response.user?.userID) {
            localStorage.setItem(this.userIdKey, String(response.user.userID));
          }
        }),
        catchError(this.handleError)
      );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, request, { responseType: 'text' })
      .pipe(
        tap(response => console.log('Register response:', response)),
        catchError(error => {
          // If it's a successful status code (2xx) with parsing error, treat as success
          if (error.status === 201 || error.status === 200) {
            console.log('Register successful (empty response)');
            return throwError(() => new Error('success'));
          }
          return this.handleError(error);
        })
      );
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, request, { responseType: 'text' })
      .pipe(
        tap(response => console.log('Forgot password response:', response)),
        catchError(error => {
          // If it's a successful status code (2xx) with parsing error, treat as success
          if (error.status === 201 || error.status === 200) {
            console.log('Forgot password successful (empty response)');
            return throwError(() => new Error('success'));
          }
          return this.handleError(error);
        })
      );
  }

  resetPassword(request: ResetPasswordRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, request, { responseType: 'text' })
      .pipe(
        tap(response => console.log('Reset password response:', response)),
        catchError(error => {
          // If it's a successful status code (2xx) with parsing error, treat as success
          if (error.status === 201 || error.status === 200) {
            console.log('Reset password successful (empty response)');
            return throwError(() => new Error('success'));
          }
          return this.handleError(error);
        })
      );
  }

  confirmAccount(token: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/confirm-account`, { rawToken: token }, { responseType: 'text' })
      .pipe(
        tap(response => console.log('Confirm account response:', response)),
        catchError(error => {
          // If it's a successful status code (2xx) with parsing error, treat as success
          if (error.status === 201 || error.status === 200) {
            console.log('Account confirmed successfully (empty response)');
            return throwError(() => new Error('success'));
          }
          return this.handleError(error);
        })
      );
  }

  private handleError(error: HttpErrorResponse) {
    // Only log actual HTTP errors (4xx, 5xx)
    console.error('HTTP Error:', error.status, error.message);
    return throwError(() => new Error('Something went wrong; please try again later.'));
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  clearToken(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userIdKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUserId(): number | null {
    const storedUserId = localStorage.getItem(this.userIdKey);
    if (storedUserId) {
      const numericId = Number(storedUserId);
      if (Number.isFinite(numericId)) {
        return numericId;
      }
    }

    const token = this.getToken();
    if (!token) {
      return null;
    }

    const payload = this.decodeJwtPayload(token);
    if (!payload) {
      return null;
    }

    const rawId = payload['userID'] ?? payload['userId'] ?? payload['id'] ?? payload['sub'];
    const numericId = typeof rawId === 'string' ? Number(rawId) : Number(rawId);
    return Number.isFinite(numericId) ? numericId : null;
  }

  private decodeJwtPayload(token: string): Record<string, unknown> | null {
    const parts = token.split('.');
    if (parts.length < 2) {
      return null;
    }

    const normalized = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized.padEnd(normalized.length + (4 - (normalized.length % 4)) % 4, '=');

    try {
      return JSON.parse(atob(padded)) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}