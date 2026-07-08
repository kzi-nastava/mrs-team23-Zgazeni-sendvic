import { RideStatus } from '../models/hor.models';

export interface Location {
  latitude: number;
  longitude: number;
}

export interface RideSummaryDTO {
  rideCount: number;
  totalDistanceKm: number;
  totalDurationMinutes: number;
  totalPrice: number;
}

export interface DailyRideReportDTO {
  date: string;
  rideCount: number;
  distanceKm: number;
  money: number;
}

export interface RideReportDTO {
    dailyReports: DailyRideReportDTO[];
    summary: RideSummaryDTO;
}