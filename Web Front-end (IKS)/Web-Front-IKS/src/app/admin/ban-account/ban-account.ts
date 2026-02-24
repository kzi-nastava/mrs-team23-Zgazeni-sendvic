import { Component, ChangeDetectionStrategy, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { finalize, catchError, of, timeout } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

type AccountAdminViewDTO = {
  id: number;
  email: string;
  name: string;
  lastName: string;
  phoneNumber: string;
  address: string;
  confirmed: boolean;
  banned: boolean;
  accountType: string; // e.g. ADMIN/USER/DRIVER from discriminator
};

type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 0-based
};

@Component({
  selector: 'app-ban-account',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './ban-account.html',
  styleUrls: ['./ban-account.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BanAccount implements OnInit {

  // adjust if your controller base path differs
  private readonly apiBase = 'http://localhost:8080/api/account';

  loading = false;
  errorMsg: string | null = null;

  accounts: AccountAdminViewDTO[] = [];

  // paging
  pageIndex = 0;
  pageSize = 20;
  totalPages = 0;
  totalElements = 0;

  // filters (match backend params: q, type, confirmed)
  q = '';
  type: '' | 'USER' | 'DRIVER' | 'ADMIN' = '';
  confirmed: '' | 'true' | 'false' = '';
  sortBy: 'id' | 'email' | 'isConfirmed' = 'id';
  sortDir: 'asc' | 'desc' = 'desc';

  banningIds = new Set<number>();

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number) {
    this.loading = true;
    this.errorMsg = null;
    this.cdr.markForCheck();

    let params = new HttpParams()
      .set('page', page)
      .set('size', this.pageSize)
      .set('sort', `${this.sortBy},${this.sortDir}`);

    if (this.q.trim()) params = params.set('q', this.q.trim());
    if (this.type) params = params.set('type', this.type);
    if (this.confirmed) params = params.set('confirmed', this.confirmed);

    this.http.get<PageResponse<AccountAdminViewDTO>>(`${this.apiBase}/all`, { params }).pipe(
      timeout(8000),
      catchError(() => {
        this.errorMsg = 'Failed to load accounts.';
        const empty: PageResponse<AccountAdminViewDTO> = {
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: this.pageSize,
          number: page
        };
        return of(empty);
      }),
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe((res) => {
      this.accounts = res.content ?? [];
      this.totalElements = res.totalElements ?? 0;
      this.totalPages = res.totalPages ?? 0;
      this.pageIndex = res.number ?? page;
      this.cdr.markForCheck();
    });
  }

  applyFilters() {
    this.loadPage(0);
  }

  clearFilters() {
    this.q = '';
    this.type = '';
    this.confirmed = '';
    this.loadPage(0);
  }

  prevPage() {
    if (this.pageIndex > 0) this.loadPage(this.pageIndex - 1);
  }

  nextPage() {
    if (this.pageIndex + 1 < this.totalPages) this.loadPage(this.pageIndex + 1);
  }

  banAccount(a: AccountAdminViewDTO) {
    if (!a?.id || a.banned) return;

    const ok = confirm(`Ban account ${a.email}?`);
    if (!ok) return;

    this.banningIds.add(a.id);
    this.cdr.markForCheck();

    this.http.put(`${this.apiBase}/ban/${a.id}`, null, { responseType: 'text' }).pipe(
      timeout(8000),
      catchError(() => {
        alert('Failed to ban account.');
        return of(null);
      }),
      finalize(() => {
        this.banningIds.delete(a.id);
        this.cdr.markForCheck();
      })
    ).subscribe(() => {
      // update UI immediately
      a.banned = true;
      this.cdr.markForCheck();
    });
  }

  isBanning(id: number) {
    return this.banningIds.has(id);
  }
}
