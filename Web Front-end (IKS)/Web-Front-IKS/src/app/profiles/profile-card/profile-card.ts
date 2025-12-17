import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-profile-card',
  standalone: true,
  templateUrl: './profile-card.html',
  styleUrls: ['./profile-card.css']
})
export class ProfileCard {
  @Input() displayName = '';
  @Input() name = '';
  @Input() surname = '';
  @Input() dob = '';
  @Input() email = '';
  @Input() phone = '';
  @Input() password = '';
}
