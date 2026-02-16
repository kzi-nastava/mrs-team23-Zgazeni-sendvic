import { Location } from '../model/route';

export enum RideStatus {
  SCHEDULED = 'SCHEDULED',
  ACTIVE = 'ACTIVE',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED'
}

export interface ARideRequestedDTO {
  rideID: number;
  destinations: Location[];
  arrivingPoint: Location;
  endingPoint: Location;
  beginning: string;
  ending: string;
  creationTime: string;
  status: RideStatus;
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

export interface HORAccountDetailsDTO {
  email: string;
  firstName: string;
  lastName: string;
  accountId: number;
}

export interface ARideDetailsNoteDTO {
  noteId: number;
  userId: number;
  note: string;
}

export interface RideDriverRatingDTO {
  userId: number;
  rideId: number;
  driverRating: number;
  vehicleRating: number;
  comment: string;
}

export interface ARideDetailsRequestedDTO {
  passengers: HORAccountDetailsDTO[];
  driver: HORAccountDetailsDTO;
  rideNotes: ARideDetailsNoteDTO[];
  rideDriverRatings: RideDriverRatingDTO[];
  arrivingPoint?: Location;
  endingPoint?: Location;
  destinations?: Location[];
}

export interface ARideRequestedUserDTO {
  rideID: number;
  destinations: Location[];
  beginning: string;
  ending: string;
  creationTime: string;
}
