import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'future-rides',
  imports: [RouterModule],
  templateUrl: './future-rides.html',
  styleUrl: './future-rides.css',
})
export class FutureRides {
  rides = [
    { id: 1, origin: 'Location A', destination: 'Location B', date: '2026-02-15', timeStart: '10:00 AM' },
    { id: 2, origin: 'Location C', destination: 'Location D', date: '2026-02-20', timeStart: '02:00 PM' }
  ];
}
