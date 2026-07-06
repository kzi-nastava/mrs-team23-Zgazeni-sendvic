import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { RideReportDTO } from '../models/ride-report.model';

@Injectable({ providedIn: 'root' })
export class RideReportService {

  private baseUrl = 'http://localhost:8080/api/HOR';

  constructor(private http: HttpClient) {}

  getReport(from?: string, to?: string): Observable<RideReportDTO> {

    return of<RideReportDTO>({

    summary: {
      rideCount: 6,
      totalDistanceKm: 52.8,
      totalDurationMinutes: 94,
      totalPrice: 3180
    },

    rides: [

      {
        rideId: 1,
        driverName: "John Smith",
        startTime: "2026-06-20T12:15:00",
        endTime: "2026-06-20T12:35:00",
        startLocation: {
          latitude: 45.251,
          longitude: 19.845
        },
        destinationLocation: {
          latitude: 45.260,
          longitude: 19.810
        },
        distanceKm: 9.7,
        durationMinutes: 20,
        totalPrice: 620,
        status: "FINISHED"
      },

      {
        rideId: 2,
        driverName: "Jane Doe",
        startTime: "2026-06-21T09:10:00",
        endTime: "2026-06-21T09:26:00",
        startLocation: {
          latitude: 45.245,
          longitude: 19.842
        },
        destinationLocation: {
          latitude: 45.268,
          longitude: 19.865
        },
        distanceKm: 7.5,
        durationMinutes: 16,
        totalPrice: 470,
        status: "FINISHED"
      },

      {
        rideId: 3,
        driverName: "John Smith",
        startTime: "2026-06-22T15:40:00",
        endTime: "2026-06-22T16:05:00",
        startLocation: {
          latitude: 45.250,
          longitude: 19.830
        },
        destinationLocation: {
          latitude: 45.282,
          longitude: 19.878
        },
        distanceKm: 12.8,
        durationMinutes: 25,
        totalPrice: 790,
        status: "FINISHED"
      },

      {
        rideId: 4,
        driverName: "Emily Brown",
        startTime: "2026-06-24T08:20:00",
        endTime: "2026-06-24T08:34:00",
        startLocation: {
          latitude: 45.257,
          longitude: 19.849
        },
        destinationLocation: {
          latitude: 45.240,
          longitude: 19.820
        },
        distanceKm: 6.1,
        durationMinutes: 14,
        totalPrice: 390,
        status: "FINISHED"
      },

      {
        rideId: 5,
        driverName: "Michael Johnson",
        startTime: "2026-06-25T17:45:00",
        endTime: "2026-06-25T18:08:00",
        startLocation: {
          latitude: 45.247,
          longitude: 19.870
        },
        destinationLocation: {
          latitude: 45.231,
          longitude: 19.832
        },
        distanceKm: 10.3,
        durationMinutes: 23,
        totalPrice: 610,
        status: "FINISHED"
      },

      {
        rideId: 6,
        driverName: "Jane Doe",
        startTime: "2026-06-27T20:15:00",
        endTime: "2026-06-27T20:31:00",
        startLocation: {
          latitude: 45.249,
          longitude: 19.846
        },
        destinationLocation: {
          latitude: 45.272,
          longitude: 19.885
        },
        distanceKm: 6.4,
        durationMinutes: 16,
        totalPrice: 300,
        status: "CANCELED"
      }

    ]

  } as RideReportDTO);
  }
}