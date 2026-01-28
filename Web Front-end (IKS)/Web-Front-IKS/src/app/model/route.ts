export interface Location {
  latitude: number;
  longitude: number;
}

export interface Route {
  id: number;
  locations: Location[];
}
