import { Component, ChangeDetectionStrategy, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize, catchError, of } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

import { BaseChartDirective } from 'ng2-charts';
import { ReportService } from '../service/report.service';
import { AccountService } from '../service/account.service';

type Point = { day: string; ridesCount: number; kilometers: number; money: number };
type Summary = {
  totalRides: number;
  totalKilometers: number;
  totalMoney: number;
  avgRidesPerDay: number;
  avgKilometersPerDay: number;
  avgMoneyPerDay: number;
  avgKilometersPerRide: number;
  avgMoneyPerRide: number;
};

@Component({
  selector: 'app-ride-report',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    BaseChartDirective
  ],
  templateUrl: './ride-report.html',
  styleUrls: ['./ride-report.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RideReport implements OnInit {

  loading = false;
  errorMsg: string | null = null;

  // date range
  fromDate!: Date;
  toDate!: Date;

  // role-aware scope
  isAdmin = false;
  scope: 'ME' | 'ALL_DRIVERS' | 'ALL_PASSENGERS' | 'ACCOUNT' = 'ME';
  accountId?: number;

  points: Point[] = [];
  summary?: Summary;

  // charts
  ridesLabels: string[] = [];
  ridesData: number[] = [];

  kmLabels: string[] = [];
  kmData: number[] = [];

  moneyLabels: string[] = [];
  moneyData: number[] = [];

  constructor(
    private reportService: ReportService,
    private accountService: AccountService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // default: last 7 days
    const now = new Date();
    this.toDate = now;
    this.fromDate = new Date(now);
    this.fromDate.setDate(now.getDate() - 6);

    // detect admin
    this.accountService.getMyAccount().subscribe({
      next: (acc: { role: string; }) => {
        this.isAdmin = (acc.role === 'ADMIN');
        if (!this.isAdmin) this.scope = 'ME';
        this.load();
      },
      error: () => {
        // even if profile fails, allow ME
        this.load();
      }
    });
  }

  load() {
    this.loading = true;
    this.errorMsg = null;
    this.cdr.markForCheck();

    const from = this.toIsoDate(this.fromDate);
    const to = this.toIsoDate(this.toDate);

    const scope = this.isAdmin ? this.scope : 'ME';
    const accId = (scope === 'ACCOUNT') ? this.accountId : undefined;

    this.reportService.getRideReport(from, to, scope, accId).pipe(
      catchError((err) => {
        this.errorMsg = err?.error?.message ?? 'Failed to load report.';
        return of({ points: [], summary: null });
      }),
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    ).subscribe((res: { points: never[]; summary: undefined; }) => {
      this.points = res.points ?? [];
      this.summary = res.summary ?? undefined;

      this.buildCharts();
      this.cdr.markForCheck();
    });
  }

  private buildCharts() {
    const labels = this.points.map(p => p.day);

    this.ridesLabels = labels;
    this.ridesData = this.points.map(p => p.ridesCount);

    this.kmLabels = labels;
    this.kmData = this.points.map(p => p.kilometers);

    this.moneyLabels = labels;
    this.moneyData = this.points.map(p => p.money);
  }

  apply() {
    if (!this.fromDate || !this.toDate) return;
    if (this.scope === 'ACCOUNT' && !this.accountId) {
      alert('Please enter accountId for ACCOUNT scope.');
      return;
    }
    this.load();
  }

  private toIsoDate(d: Date): string {
    // yyyy-mm-dd
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}