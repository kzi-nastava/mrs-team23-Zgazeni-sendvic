import { Location } from '../model/route';

export interface ARideRequestedDTO {
  rideID: number;
  destinations: Location[];
  arrivingPoint: Location;
  endingPoint: Location;
  beginning: string;
  ending: string;
  status: string;
  whoCancelled: number | null;
  price: number;
  panic: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort?: unknown;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
  empty?: boolean;
}
