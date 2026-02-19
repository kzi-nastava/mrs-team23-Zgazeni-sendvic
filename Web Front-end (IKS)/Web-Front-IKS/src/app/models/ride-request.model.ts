export enum VehicleType {
  STANDARD = 'STANDARD',
  VAN = 'VAN',
  LUXURY = 'LUXURY'
}

export interface RideRequestDTO {
  locations: Location[];
  vehicleType: VehicleType;
  babiesAllowed: boolean;
  petsAllowed: boolean;
  scheduledTime: string | null;
  invitedPassengers: number[];   // IDs of accounts
  estimatedDistanceKm: number;
}
