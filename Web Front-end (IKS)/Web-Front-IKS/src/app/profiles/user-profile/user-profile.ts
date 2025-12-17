import { Component } from '@angular/core';
import { ProfileCard } from '../../profiles/profile-card/profile-card';

@Component({
  standalone: true,
  selector: 'app-user-profile',
  imports: [ProfileCard],
  templateUrl: './user-profile.html',
  styleUrls: ['./user-profile.css']
})
export class UserProfile {}