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

@Component({
  selector: 'app-map',
  standalone: true,
  templateUrl: './map.html',
  styleUrl: './map.css'
})
export class Map implements AfterViewInit, OnDestroy {

  // MODE CONTROL
  @Input() showMultipleVehicles: boolean = false;

  // ROUTE INPUTS (used when showMultipleVehicles = false)
  @Input() pickup?: L.LatLngTuple;
  @Input() destination?: L.LatLngTuple;

  @Output() mapClicked = new EventEmitter<{ lat: number; lng: number }>();

  private mapInstance!: L.Map;
  private routingControl: any;

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

    if (this.showMultipleVehicles) {
      this.renderFleet();
    } else if (this.pickup && this.destination) {
      this.drawRoute(this.pickup, this.destination);
    }
  }

  // ---------------------------
  // FLEET MODE (Home Page)
  // ---------------------------
  private renderFleet(): void {
    const vehicleMarkers: { coords: L.LatLngTuple; popup: string }[] = [
      { coords: [45.245, 19.816], popup: 'Vehicle 1<br>Available' },
      { coords: [45.230, 19.829], popup: 'Vehicle 2<br>Available' },
      { coords: [45.238, 19.811], popup: 'Vehicle 3<br>Not Available' }
    ];

    vehicleMarkers.forEach(vehicle => {
      L.marker(vehicle.coords)
        .addTo(this.mapInstance)
        .bindPopup(vehicle.popup);
    });
  }

  // ---------------------------
  // ROUTE MODE (Ride Tracking)
  // ---------------------------
  private drawRoute(start: L.LatLngTuple, end: L.LatLngTuple): void {

    this.routingControl = (L as any).Routing.control({
      waypoints: [
        L.latLng(start[0], start[1]),
        L.latLng(end[0], end[1])
      ],
      routeWhileDragging: false,
      addWaypoints: false,
      draggableWaypoints: false,
      show: false,
      lineOptions: {
        styles: [{ color: '#1976d2', weight: 5 }]
      }
    }).addTo(this.mapInstance);

    // Optional: Extract distance & duration
    this.routingControl.on('routesfound', (e: any) => {
      const route = e.routes[0];
      const distanceKm = route.summary.totalDistance / 1000;
      const durationMin = route.summary.totalTime / 60;

      console.log('Distance (km):', distanceKm);
      console.log('Duration (min):', durationMin);
    });
  }
}
