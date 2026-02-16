import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, filter } from 'rxjs';
import { GetAccountDTO } from '../models/account.dto';

@Injectable({ providedIn: 'root' })
export class AccountService {

  private readonly apiUrl = 'http://localhost:8080/api/account';

  // BehaviorSubject to store current account data
  private accountSubject = new BehaviorSubject<GetAccountDTO | null>(null);
  account$ = this.accountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Fetch account from backend, store in BehaviorSubject
  getMyAccount(): Observable<GetAccountDTO> {
    // If we already have the data, just return the observable
    if (this.accountSubject.value) {
      return this.account$.pipe(
        filter((acc): acc is GetAccountDTO => acc !== null)
      );
    }

    // Otherwise fetch from API
    return this.http.get<GetAccountDTO>(`${this.apiUrl}/me`)
      .pipe(
        tap(acc => this.accountSubject.next(acc))
      );
  }

  // Force reload account from backend
  reloadAccount(): Observable<GetAccountDTO> {
    return this.http.get<GetAccountDTO>(`${this.apiUrl}/me`)
      .pipe(
        tap(acc => this.accountSubject.next(acc))
      );
  }

  // Update account (kept your existing method)
  updateAccount(dto: any) {
    return this.http.put(`${this.apiUrl}/me/change-request`, dto, { responseType: 'text' });
  }
  
}
