import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { BaseChartDirective } from 'ng2-charts';
import {
  Chart,
  registerables,
  ChartData,
  ChartOptions
} from 'chart.js';
import { RideReportDTO, DailyRideReportDTO, RideSummaryDTO } from '../models/ride-report.model';
import { RideReportService } from '../service/ride-report.service';
import { AuthService } from '../service/auth.service';
import { AccountService } from '../service/account.service';
import { AccountAdminViewDTO } from '../models/account.dto';
import { MatSelectModule } from '@angular/material/select';

Chart.register(...registerables);

@Component({
  selector: 'app-rides-report',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    BaseChartDirective,
    MatSelectModule
],
  templateUrl: './rides-report.html',
  styleUrl: './rides-report.css'
})
export class RidesReport {

  fromDate: Date | null = null;
  toDate: Date | null = null;

  loading = signal(false);

  isAdmin = false;

  selectedUserId:number|null = null;

  constructor(
    private reportService:RideReportService,
    private authService:AuthService,
    private accountService: AccountService
  ){}

  users: AccountAdminViewDTO[] = [];

  dailyReports = signal<DailyRideReportDTO[]>([]);
  summary: RideSummaryDTO | null = null;

  ngOnInit() {
    this.isAdmin =
      this.authService.getRole() === 'ADMIN';

    if (this.isAdmin) {
        this.loadUsers();
    }
    
    this.loadReport();
  }

  private loadUsers() {

    this.accountService
        .getAllAccounts()
        .subscribe({
          next: page => {
            this.users = page.content;
          },
          error: err => {
            console.error(err);
          }
        });
  }

  loadReport(){

    this.loading.set(true);

    this.reportService.getReport()
      .subscribe({

        next: report => {

          this.summary = report.summary;

          this.dailyReports.set(report.dailyReports);

          this.updateCharts();

          this.loading.set(false);

        },

        error: err => {

          console.error(err);

          this.loading.set(false);

        }

      });
  }

  // ===== SUMMARY (computed from rides) =====
  get totalRides() {
      return this.summary?.rideCount ?? 0;
  }

  get totalDistance() {
      return this.summary?.totalDistanceKm ?? 0;
  }

  get totalRevenue() {
      return this.summary?.totalPrice ?? 0;
  }

  get avgDuration() {

      if (!this.summary || this.summary.rideCount === 0)
          return 0;

      return this.summary.totalDurationMinutes /
            this.summary.rideCount;
  }

  // ===== FILTER (mock behavior) =====
  applyFilters() {
    const from =
        this.fromDate?.toISOString();

    const to =
        this.toDate?.toISOString();

    this.loading.set(true);

    this.reportService
    .getReport(
        from,
        to,
        this.selectedUserId ?? undefined
    )
    .subscribe(report => {
        this.summary = report.summary;
        this.dailyReports.set(report.dailyReports);
        this.updateCharts();
        this.loading.set(false);
    });
  }

  clearFilters() {
    this.fromDate = null;
    this.toDate = null;
  }

  // ===== CHART DATA =====
  distanceChartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        label: 'Distance (km)',
        data: []
      }
    ]
  };

  distanceChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    plugins: {
      legend: {
        display: true
      }
    }
  };

  revenueChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        label: 'Price',
        data: [],
        tension: 0.3
      }
    ]
  };

  revenueChartOptions: ChartOptions<'line'> = {
    responsive: true,
    plugins: {
      legend: {
        display: true
      }
    }
  };

  private updateCharts(){
    const data = this.dailyReports();

    this.distanceChartData = {
        labels:
          data.map(x => x.date),
        datasets:[
          {
            label:'Distance (km)',
            data:
              data.map(x => x.distanceKm)
          }
        ]
    };

    this.revenueChartData = {
        labels:
          data.map(x => x.date),
        datasets:[
          {
            label:'Price (RSD)',
            data:
              data.map(x => x.money),
            tension:0.3
          }
        ]
    };
  }
}