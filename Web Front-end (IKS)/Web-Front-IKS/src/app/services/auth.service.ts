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
  private apiUrl = 'http://localhost:8080/api/auth'; // Adjust your backend URL

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => console.log('Login response:', response)),
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
}