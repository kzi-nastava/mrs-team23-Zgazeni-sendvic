import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PanelService {
  showEstimationPanel = signal(false);

  togglePanel() {
    this.showEstimationPanel.set(!this.showEstimationPanel());
  }

  hidePanel() {
    this.showEstimationPanel.set(false);
  }
}
