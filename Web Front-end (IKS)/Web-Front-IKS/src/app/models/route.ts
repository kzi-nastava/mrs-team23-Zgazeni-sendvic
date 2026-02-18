export interface Location {
  latitude: number;
  longitude: number;
  address: string;
}

export interface Route {
  id: number;
  locations: Location[];
}
