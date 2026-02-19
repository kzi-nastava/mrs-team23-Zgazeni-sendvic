import { Location } from './route.dto';

export interface RouteEstimationRequest {
    locations: Location[];
    }


export interface RouteEstimationResponse {
  distanceMeters: number;
  durationSeconds: number;
  pathCoordinates: number[][]; // Array of [longitude, latitude] pairs
  Type: string;
  distanceKm: number;
  durationMinutes: number;
  price: number;
}