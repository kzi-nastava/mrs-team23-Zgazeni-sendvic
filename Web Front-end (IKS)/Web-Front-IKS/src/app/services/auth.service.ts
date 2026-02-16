import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  PictureUploadResponse,
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
  private picturesUrl = 'http://localhost:8080/api/pictures';
  private tokenKey = 'jwt_token';
  private roleKey = 'user_role';
  private profilePictureKey = 'profile_picture';

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          console.log('Login response:', response);
          if (response.token) {
            localStorage.setItem(this.tokenKey, response.token);
          }
          if (response.user?.role) {
            localStorage.setItem(this.roleKey, response.user.role);
          }
        }),
        catchError(this.handleError)
      );
  }

  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, request)
      .pipe(
        tap(response => console.log('Register response:', response)),
        catchError(this.handleError)
      );
  }

  uploadProfilePicture(file: File, pictureToken: string): Observable<PictureUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('pictureToken', pictureToken);

    return this.http.post<PictureUploadResponse>(`${this.picturesUrl}/register/profile`, formData)
      .pipe(
        tap(response => console.log('Picture upload response:', response)),
        catchError(this.handleError)
      );
  }

  fetchProfilePicture(token?: string): Observable<Blob> {
    const authToken = token ?? this.getToken();
    const headers = authToken ? new HttpHeaders({ Authorization: `Bearer ${authToken}` }) : undefined;

    return this.http.get(`${this.picturesUrl}/retrieve/profile`, {
      headers,
      responseType: 'blob'
    }).pipe(
      tap(() => console.log('Profile picture retrieved')),
      catchError(this.handleError)
    );
  }

  storeProfilePicture(blob: Blob): void {
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        localStorage.setItem(this.profilePictureKey, reader.result);
      }
    };
    reader.readAsDataURL(blob);
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

  getRole(): string | null {
    return localStorage.getItem(this.roleKey);
  }

  clearToken(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    localStorage.removeItem(this.profilePictureKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}