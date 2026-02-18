import {
  Component,
  AfterViewInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter
} from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';

export interface VehiclePosition {
  latitude: number;
  longitude: number;
  vehicleId?: number;
  status?: string;
}

interface RideMapUpdate {
  current: L.LatLngTuple;
  destination?: L.LatLngTuple;
  route?: L.LatLngTuple[];
}

export interface RouteMetrics {
  distanceMeters: number;
  durationSeconds?: number;
}


@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.html',
  styleUrl: './map.css'
})
export class Map implements AfterViewInit, OnDestroy {

  // MODE CONTROL
  @Input() showMultipleVehicles: boolean = false;
  @Output() routeMetrics = new EventEmitter<RouteMetrics>();

  // ROUTE INPUTS (used when showMultipleVehicles = false)
  @Input() pickup?: L.LatLngTuple;
  @Input() destination?: L.LatLngTuple;

  @Output() mapClicked = new EventEmitter<{ lat: number; lng: number }>();

  private mapInstance!: L.Map;
  private routingControl: any;
  private vehicleLayer: L.LayerGroup | null = null;
  private rideLayer: L.LayerGroup | null = null;
  private vehicleMarker: L.Marker | null = null;
  private destinationMarker: L.Marker | null = null;
  private routeLine: L.Polyline | null = null;
  private pendingVehicleMarkers: VehiclePosition[] | null = null;
  private pendingRideUpdate: RideMapUpdate | null = null;

  constructor() {}

  ngAfterViewInit(): void {
    this.initializeMap();

    setTimeout(() => {
      this.mapInstance.invalidateSize();
    }, 0);
  }

  ngOnDestroy(): void {
    if (this.mapInstance) {
      this.mapInstance.remove();
    }
  }

  // ---------------------------
  // INITIALIZATION
  // ---------------------------
  private initializeMap(): void {
    this.mapInstance = L.map('map', {
      center: [45.2396, 19.8227],
      zoom: 13,
      zoomControl: false
    });

    this.mapInstance.on('click', (e: L.LeafletMouseEvent) => {
      this.mapClicked.emit({
        lat: e.latlng.lat,
        lng: e.latlng.lng
      });
    });

    L.control.zoom({ position: 'topright' }).addTo(this.mapInstance);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(this.mapInstance);

    this.vehicleLayer = L.layerGroup().addTo(this.mapInstance);
    this.rideLayer = L.layerGroup().addTo(this.mapInstance);

    if (this.pendingVehicleMarkers) {
      this.setVehicleMarkersInternal(this.pendingVehicleMarkers);
      this.pendingVehicleMarkers = null;
    }

    if (this.pendingRideUpdate) {
      this.updateRideLocationInternal(this.pendingRideUpdate);
      this.pendingRideUpdate = null;
    }
  }

  setVehicleMarkers(vehicles: VehiclePosition[]): void {
    if (!this.mapInstance || !this.vehicleLayer) {
      this.pendingVehicleMarkers = vehicles;
      return;
    }

    this.setVehicleMarkersInternal(vehicles);
  }

  updateRideLocation(
    current: L.LatLngTuple,
    destination?: L.LatLngTuple,
    route?: L.LatLngTuple[]
  ): void {
    const update: RideMapUpdate = { current, destination, route };

    if (!this.mapInstance || !this.rideLayer) {
      this.pendingRideUpdate = update;
      return;
    }

    this.updateRideLocationInternal(update);
  }

  fitToBounds(points: L.LatLngTuple[]): void {
    if (!this.mapInstance || points.length === 0) {
      return;
    }

    const bounds = L.latLngBounds(points);
    this.mapInstance.fitBounds(bounds, { padding: [50, 50] });
  }

  private setVehicleMarkersInternal(vehicles: VehiclePosition[]): void {
    if (!this.vehicleLayer) {
      return;
    }

    this.vehicleLayer.clearLayers();

    vehicles.forEach((vehicle) => {
      const coords: L.LatLngTuple = [vehicle.latitude, vehicle.longitude];
      const popupParts: string[] = [];

      if (vehicle.vehicleId !== undefined) {
        popupParts.push(`Vehicle ${vehicle.vehicleId}`);
      }

      if (vehicle.status) {
        popupParts.push(`Status: ${vehicle.status}`);
      }

      const popupText = popupParts.join('<br>');
      const marker = L.marker(coords);

      if (popupText) {
        marker.bindPopup(popupText);
      }

      marker.addTo(this.vehicleLayer!);
    });
  }

  private updateRideLocationInternal(update: RideMapUpdate): void {
    if (!this.rideLayer) {
      return;
    }

    if (!this.vehicleMarker) {
      this.vehicleMarker = L.marker(update.current)
        .addTo(this.rideLayer)
        .bindPopup('Vehicle');
    } else {
      this.vehicleMarker.setLatLng(update.current);
    }

    if (update.destination) {
      if (!this.destinationMarker) {
        this.destinationMarker = L.marker(update.destination)
          .addTo(this.rideLayer)
          .bindPopup('Destination');
      } else {
        this.destinationMarker.setLatLng(update.destination);
      }

      const routing = (L as any).Routing;
      if (routing && this.mapInstance) {
        if (!this.routingControl) {
          this.routingControl = routing.control({
            waypoints: [
              L.latLng(update.current[0], update.current[1]),
              L.latLng(update.destination[0], update.destination[1]),
            ],
            lineOptions: {
              styles: [{ color: '#1976d2', weight: 4 }],
              extendToWaypoints: true,
            },
            addWaypoints: false,
            draggableWaypoints: false,
            fitSelectedRoutes: false,
            show: false,
            createMarker: () => null,
          }).addTo(this.mapInstance);

          this.routingControl.on('routesfound', (event: any) => {
            const route = event?.routes?.[0];
            if (route?.summary?.totalDistance) {
              this.routeMetrics.emit({
                distanceMeters: route.summary.totalDistance,
                durationSeconds: route.summary.totalTime,
              });
            }
          });
        } else {
          this.routingControl.setWaypoints([
            L.latLng(update.current[0], update.current[1]),
            L.latLng(update.destination[0], update.destination[1]),
          ]);
        }

        if (this.routeLine) {
          this.rideLayer.removeLayer(this.routeLine);
          this.routeLine = null;
        }
      }
    } else if (this.routingControl && this.mapInstance) {
      this.mapInstance.removeControl(this.routingControl);
      this.routingControl = null;
    }

    if (!this.routingControl && update.route && update.route.length > 1) {
      if (!this.routeLine) {
        this.routeLine = L.polyline(update.route, { color: '#1976d2' })
          .addTo(this.rideLayer);
      } else {
        this.routeLine.setLatLngs(update.route);
      }
    } else if (!update.route || update.route.length <= 1) {
      if (this.routeLine) {
        this.rideLayer.removeLayer(this.routeLine);
        this.routeLine = null;
      }
    }
  }
}
