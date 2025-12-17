import { Component } from '@angular/core';
import { ProfileCard } from '../../profiles/profile-card/profile-card';

@Component({
  standalone: true,
  selector: 'app-admin-profile',
  imports: [ProfileCard],
  templateUrl: './admin-profile.html',
  styleUrls: ['./admin-profile.css']
})
export class AdminProfile {}
