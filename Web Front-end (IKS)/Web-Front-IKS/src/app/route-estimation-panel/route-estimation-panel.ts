import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-route-estimation-panel',
  imports: [CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './route-estimation-panel.html',
  styleUrl: './route-estimation-panel.css',
})
export class RouteEstimationPanel {
  estimatedTime = signal<number | null>(null);
  panelVisible = signal<boolean>(true);

  estimateRoute() {
    const randomMinutes = Math.floor(Math.random() * (15 - 3 + 1)) + 3;
    this.estimatedTime.set(randomMinutes);
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
}
