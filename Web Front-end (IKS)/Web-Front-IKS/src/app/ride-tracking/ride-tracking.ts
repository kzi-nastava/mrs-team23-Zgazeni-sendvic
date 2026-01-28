import { Component } from '@angular/core';
import { Map } from '../map/map';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ride-tracking',
  imports: [Map, MatButtonModule, FormsModule],
  templateUrl: './ride-tracking.html',
  styleUrl: './ride-tracking.css',
})
export class RideTracking {
  startingPoint = 'Bulevar oslobođenja 46, Novi Sad';
  destination = 'Trg slobode 1, Novi Sad';
  estimatedTime = '8 min';

  showNoteForm = false;
  showRateForm = false;
  noteText = '';
  driverRating = 5;
  vehicleRating = 5;
  ratingComment = '';

  stopRide(): void {
    console.log('Stop Ride clicked');
  }

  openNote(): void {
    this.showNoteForm = true;
    this.showRateForm = false;
  }

  openRate(): void {
    this.showRateForm = true;
    this.showNoteForm = false;
  }

  closeNoteForm(): void {
    this.showNoteForm = false;
    this.noteText = '';
  }

  closeRateForm(): void {
    this.showRateForm = false;
    this.driverRating = 5;
    this.vehicleRating = 5;
    this.ratingComment = '';
  }

  sendNote(): void {
    console.log('Note sent:', this.noteText);
    this.closeNoteForm();
  }

  sendRating(): void {
    console.log('Rating sent:', {
      driver: this.driverRating,
      vehicle: this.vehicleRating,
      comment: this.ratingComment,
    });
    this.closeRateForm();
  }
}
