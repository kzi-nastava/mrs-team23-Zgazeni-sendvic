import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Map } from '../../map/map';
import { RouteEstimationPanel } from '../../route-estimation-panel/route-estimation-panel';
import { RouteEstimationService } from '../../service/route.estimation.serivce';

@Component({
  selector: 'app-home',
  imports: [RouterModule, Map, RouteEstimationPanel, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  constructor(public panelService: RouteEstimationService) {}

  ngOnInit() {
    this.panelService.hidePanel();
  }
}
