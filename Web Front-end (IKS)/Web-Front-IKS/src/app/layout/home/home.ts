import { Component, AfterViewInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Map, VehiclePosition } from '../../map/map';
import { RouteEstimationPanel } from '../../route-estimation-panel/route-estimation-panel';
import { RouteEstimationService } from '../../service/route.estimation.serivce';

interface VehiclePositionsResponse {
  vehiclePositions: VehiclePosition[];
}

@Component({
  selector: 'app-home',
  imports: [RouterModule, Map, RouteEstimationPanel, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements AfterViewInit {
  @ViewChild(Map) mapComponent?: Map;

  constructor(
    private http: HttpClient,
    public panelService: RouteEstimationService
  ) {}

  ngOnInit() {
    this.panelService.hidePanel();
  }

  ngAfterViewInit(): void {
    this.loadVehiclePositions();
  }

  private loadVehiclePositions(): void {
    this.http
      .get<VehiclePositionsResponse>('http://localhost:8080/api/vehicle-positions')
      .subscribe({
        next: (response) => {
          this.mapComponent?.setVehicleMarkers(response.vehiclePositions);
        },
        error: (err) => {
          console.error('Error fetching vehicle positions:', err);
          console.error('Error status:', err.status);
          console.error('Error message:', err.message);
        },
      });
  }
}
