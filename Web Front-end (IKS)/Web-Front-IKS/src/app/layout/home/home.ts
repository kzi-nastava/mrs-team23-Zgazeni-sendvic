import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Map } from '../../map/map';
import { RouteEstimationPanel } from '../../route-estimation-panel/route-estimation-panel';
import { PanelService } from '../../service/panel.service';

@Component({
  selector: 'app-home',
  imports: [RouterModule, Map, RouteEstimationPanel, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  constructor(public panelService: PanelService) {}

  ngOnInit() {
    this.panelService.hidePanel();
  }
}
