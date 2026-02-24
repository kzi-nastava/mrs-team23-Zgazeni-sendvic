export interface RideTrackingUpdate {
  rideId: number;
  vehicleId: number;
  currentLatitude: number;
  currentLongitude: number;
  status: 'ACTIVE' | 'SCHEDULED' | 'FINISHED' | 'CANCELED';
  price: number;
  startTime: string;
  estimatedEndTime: string;
  timeLeft: string;
  route: { latitude: number; longitude: number }[];
  driver: {
    id: number;
    name: string;
    phoneNumber: string;
  };
}

export interface RideEndedNotification {
  rideId: number;
  status: 'FINISHED' | string;
  price: number | null;
  endTime: string | null;
}
