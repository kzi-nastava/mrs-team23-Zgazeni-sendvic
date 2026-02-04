import { Location } from '../model/route';



export interface RouteEstimationRequest {
    locations: Location[];
    }


export interface OrsRouteResult {
  distanceMeters: number;
  durationSeconds: number;
  pathCoordinates: number[][]; // Array of [longitude, latitude] pairs
  Type: string;
  distanceKm: number;
  durationMinutes: number;
  price: number;
}