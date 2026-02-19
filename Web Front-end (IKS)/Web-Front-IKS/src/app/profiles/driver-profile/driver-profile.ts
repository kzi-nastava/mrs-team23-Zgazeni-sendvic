import { Component } from '@angular/core';
import { ProfileCard } from '../../profiles/profile-card/profile-card';

@Component({
  standalone: true,
  selector: 'app-driver-profile',
  imports: [ProfileCard],
  templateUrl: './driver-profile.html',
  styleUrls: ['./driver-profile.css']
})
export class DriverProfile {}
