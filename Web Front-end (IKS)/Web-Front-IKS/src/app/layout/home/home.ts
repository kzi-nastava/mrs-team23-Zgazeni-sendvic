import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Map } from '../../map/map';

@Component({
  selector: 'app-home',
  imports: [RouterModule, Map],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {

}
