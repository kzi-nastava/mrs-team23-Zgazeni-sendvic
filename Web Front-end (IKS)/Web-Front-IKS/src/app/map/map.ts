import { Component, AfterViewInit, Input } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-map',
  imports: [],
  templateUrl: './map.html',
  styleUrl: './map.css',
})
export class Map implements AfterViewInit {
  @Input() showMultipleVehicles: boolean = false;
  private mapInstance: L.Map | null = null;

  ngAfterViewInit(): void {
    this.initializeMap();
  }

  private initializeMap(): void {
    this.mapInstance = L.map('map', {
      center: [45.2396, 19.8227],
      zoom: 13,
      zoomControl: false,
    });

    L.control.zoom({ position: 'topright' }).addTo(this.mapInstance);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19,
    }).addTo(this.mapInstance);

    if (this.showMultipleVehicles) {
      const vehicleMarkers: { coords: L.LatLngTuple; popup: string }[] = [
        { coords: [45.245, 19.816], popup: 'Vehicle 1<br>Available' },
        { coords: [45.230, 19.829], popup: 'Vehicle 2<br>Available' },
        { coords: [45.238, 19.811], popup: 'Vehicle 3<br>Not Available' },
      ];
      vehicleMarkers.forEach((vehicle) => {
        L.marker(vehicle.coords)
          .addTo(this.mapInstance!)
          .bindPopup(vehicle.popup);
      });
    } else {
      const vehicleCoords: L.LatLngTuple = [45.245, 19.816];
      const destinationCoords: L.LatLngTuple = [45.230, 19.829];

      L.marker(vehicleCoords)
        .addTo(this.mapInstance)
        .bindPopup('Vehicle<br>Driving')
        .openPopup();

      L.marker(destinationCoords)
        .addTo(this.mapInstance)
        .bindPopup('Destination');
    }
  }
}
