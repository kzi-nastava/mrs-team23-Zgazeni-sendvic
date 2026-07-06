import {
  Component,
  AfterViewInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
  effect
} from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { RouteEstimationService } from '../service/route.estimation.service';
import { HttpClient } from '@angular/common/http';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { OnChanges, SimpleChanges } from '@angular/core';
const markerIcon = 'assets/app/marker-icon.png';
const markerIcon2x = 'assets/app/marker-icon-2x.png';
const markerShadow = 'assets/app/marker-shadow.png';

// Fix for Leaflet marker icons not loading
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

export interface MapClickResult {
  lat: number;
  lng: number;
  label?: string;
}

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
  styleUrls: ['./map.css']
})
export class Map implements AfterViewInit, OnDestroy, OnChanges {

  // MODE CONTROL
  @Input() showMultipleVehicles: boolean = false;
  @Output() routeMetrics = new EventEmitter<RouteMetrics>();

  // ROUTE INPUTS (used when showMultipleVehicles = false)
  @Input() pickup?: L.LatLngTuple;
  @Input() destination?: L.LatLngTuple;
  @Input() markers: L.LatLngTuple[] = [];
  @Input() route: L.LatLngTuple[] | null = null;

  @Output() mapClick = new EventEmitter<{ lat: number; lng: number }>();

  private mapInstance: L.Map | null = null;
  private routingControl: any;
  private vehicleLayer: L.LayerGroup | null = null;
  private rideLayer: L.LayerGroup | null = null;
  private vehicleMarker: L.Marker | null = null;
  private destinationMarker: L.Marker | null = null;
  private routeLine: L.Polyline | null = null;
  private pendingVehicleMarkers: VehiclePosition[] | null = null;
  private pendingRideUpdate: RideMapUpdate | null = null;
  private pendingRouteLine: L.LatLngTuple[] | null = null;
  private isMapReady = false;

  constructor(private http: HttpClient) {}

  ngAfterViewInit(): void {
    this.initializeMap();
    this.isMapReady = true;

    setTimeout(() => {
      this.mapInstance?.invalidateSize(true);
      this.mapInstance?.setView(this.mapInstance.getCenter(), this.mapInstance.getZoom());
    }, 100);
  }

  ngOnChanges(): void {
    if (!this.isMapReady) return;
    if (!this.rideLayer) return;

    this.rideLayer.clearLayers();

    if (this.pickup && this.pickup[0] !== 0 && this.pickup[1] !== 0) {
      L.marker(this.pickup).addTo(this.rideLayer);
    }

    if (this.destination && this.destination[0] !== 0 && this.destination[1] !== 0) {
      L.marker(this.destination).addTo(this.rideLayer);
    }

    if (this.route && this.route.length > 1) {
      this.setRouteLine(this.route);
    }
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

    this.mapInstance.on('click', (e) => {
      this.mapClick.emit({
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

    if (this.pendingRouteLine) {
      this.setRouteLineInternal(this.pendingRouteLine);
      this.pendingRouteLine = null;
    }
  }

  private reverseGeocode(lat: number, lon: number) {
    const url =
      `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lon}`;

    return this.http.get<any>(url).pipe(
      map(res => res?.display_name ?? null),
      catchError(() => of(null))
    );
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

  setRouteLine(route: L.LatLngTuple[] | null): void {
    if (!this.mapInstance || !this.rideLayer) {
      this.pendingRouteLine = route;
      return;
    }

    this.setRouteLineInternal(route);
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

  private setRouteLineInternal(route: L.LatLngTuple[] | null): void {
    if (!this.rideLayer) {
      return;
    }

    if (this.routeLine) {
      this.rideLayer.removeLayer(this.routeLine);
      this.routeLine = null;
    }

    if (route && route.length > 1) {
      this.routeLine = L.polyline(route, { color: '#1976d2', weight: 4 })
        .addTo(this.rideLayer);
    }
  }
}
