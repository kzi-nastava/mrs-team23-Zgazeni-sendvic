export interface Location {
  latitude: number;
  longitude: number;
  address: string;
}

export interface Route {
  id: number;
  locations: Location[];
}

export interface LocationDTO {
  latitude: number;
  longitude: number;
}

export interface RouteDTO {
  id: number;
  start: LocationDTO;
  destination: LocationDTO;
  midPoints: LocationDTO[];
}