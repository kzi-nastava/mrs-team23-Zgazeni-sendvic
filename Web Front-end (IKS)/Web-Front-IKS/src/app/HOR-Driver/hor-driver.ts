import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'hor-driver',
  imports: [ RouterModule],
  templateUrl: './hor-driver.html',
  styleUrl: './hor-driver.css',
})
export class HorDriver {

  rides = [
    { id: 1, origin: 'Location A', destination: 'Location B', date: '2024-07-01', timeStart: '10:00 AM', timeEnd: '11:00 AM', price: 15.00, canceled: 'Peter123', panic: false },
    { id: 2, origin: 'Location C', destination: 'Location D', date: '2024-07-02', timeStart: '02:00 PM', timeEnd: '03:00 PM', price: 20.00, canceled: '', panic: false },
    { id: 3, origin: 'Location E', destination: 'Location F', date: '2024-07-03', timeStart: '09:00 AM', timeEnd: '10:00 AM', price: 12.50, canceled: '', panic: true }
  ];
  
}
