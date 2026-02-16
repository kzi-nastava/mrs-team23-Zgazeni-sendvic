import { Component, Inject, ChangeDetectionStrategy, AfterViewInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ARideDetailsRequestedDTO } from '../../models/hor.models';
import { Map } from '../../map/map';

@Component({
  selector: 'app-detailed-hor-admin',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatTabsModule,
    MatCardModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    MatTooltipModule,
    Map
  ],
  templateUrl: './detailed-hor-admin.html',
  styleUrl: './detailed-hor-admin.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DetailedHorAdmin implements AfterViewInit {
  @ViewChild(Map) mapComponent!: Map;
  rideDetails: ARideDetailsRequestedDTO;

  constructor(
    public dialogRef: MatDialogRef<DetailedHorAdmin>,
    @Inject(MAT_DIALOG_DATA) public data: ARideDetailsRequestedDTO
  ) {
    this.rideDetails = data;
  }

  ngAfterViewInit(): void {
    // Map will initialize automatically
  }

  getRatingStars(rating: number): string[] {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalf = rating % 1 >= 0.5;

    for (let i = 0; i < fullStars; i++) {
      stars.push('star');
    }
    if (hasHalf) {
      stars.push('star_half');
    }
    while (stars.length < 5) {
      stars.push('star_outline');
    }
    return stars;
  }

  getAverageDriverRating(): number {
    if (!this.rideDetails.rideDriverRatings || this.rideDetails.rideDriverRatings.length === 0) {
      return 0;
    }
    const sum = this.rideDetails.rideDriverRatings.reduce((acc, rating) => acc + rating.driverRating, 0);
    return sum / this.rideDetails.rideDriverRatings.length;
  }

  getAverageVehicleRating(): number {
    if (!this.rideDetails.rideDriverRatings || this.rideDetails.rideDriverRatings.length === 0) {
      return 0;
    }
    const sum = this.rideDetails.rideDriverRatings.reduce((acc, rating) => acc + rating.vehicleRating, 0);
    return sum / this.rideDetails.rideDriverRatings.length;
  }

  onReorderClick(): void {
    console.log('Reorder ride');
    this.dialogRef.close({ reorder: true });
  }

  onCloseClick(): void {
    this.dialogRef.close();
  }
}
