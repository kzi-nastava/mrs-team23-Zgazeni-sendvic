import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import {
  RouteEstimationRequest,
  RouteEstimationResponse
} from '../models/route.estimation.models';
import { RouteEstimationService } from '../service/route.estimation.serivce';


@Component({
  selector: 'app-route-estimation-panel',
  imports: [CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, FormsModule],
  templateUrl: './route-estimation-panel.html',
  styleUrl: './route-estimation-panel.css',
})
export class RouteEstimationPanel {
  estimatedTime = signal<number | null>(null);
  panelVisible = signal<boolean>(true);
  beginningDestination = signal<string>('');
  endingDestination = signal<string>('');
  errorMessage = signal<string | null>(null);

  constructor(private routeEstimationService: RouteEstimationService) {}

  estimateRoute() {
    this.estimatedTime.set(null);
    this.routeEstimationService.setRoutePath(null);
    const beginning = this.beginningDestination();
    const ending = this.endingDestination();

    if (!beginning || !ending) {
      this.errorMessage.set('Both destinations must be chosen.');
      return;
    }

    this.errorMessage.set(null);

    this.routeEstimationService.estimateRoute({
      beginningDestination: beginning,
      endingDestination: ending
    }).subscribe({
      next: (response: RouteEstimationResponse) => {
        if (!response) {
          this.errorMessage.set('Please enter appropriate destinations.');
          this.estimatedTime.set(null);
          this.routeEstimationService.setRoutePath(null);
          return;
        }
        this.estimatedTime.set(Math.ceil(response.durationMinutes));
        this.routeEstimationService.setRoutePath(response.pathCoordinates ?? null);
        this.errorMessage.set(null);
      },
      error: (error) => {
        console.error('Error estimating route:', error);
        this.errorMessage.set('Please enter appropriate destinations.');
        this.estimatedTime.set(null);
      }
    });
  }

  togglePanelVisibility() {
    this.panelVisible.set(!this.panelVisible());
  }

  onCancel() {
    console.log('Cancel clicked');
    // Handle cancel action
  }

  onStop() {
    console.log('Stop clicked');
    // Handle stop action
  }

  onPanic() {
    console.log('PANIC clicked');
    // Handle panic action
  }
}
