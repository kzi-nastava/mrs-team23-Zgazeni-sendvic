import { RideStatus } from '../models/hor.models';

export interface Location {
  latitude: number;
  longitude: number;
}

export interface RideReportItemDTO {
  rideId: number;
  startTime: string;
  endTime: string;

  driverName: string;

  startLocation: Location;
  destinationLocation: Location;

  distanceKm: number;
  durationMinutes: number;
  totalPrice: number;

  status: RideStatus;
}

export interface RideSummaryDTO {
  rideCount: number;
  totalDistanceKm: number;
  totalDurationMinutes: number;
  totalPrice: number;
}

export interface RideReportDTO {
  rides: RideReportItemDTO[];
  summary: RideSummaryDTO;
}