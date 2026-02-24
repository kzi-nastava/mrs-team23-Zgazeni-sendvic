import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  standalone: true,
  selector: 'app-ban-reason-dialog',
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Ban account</h2>

    <div mat-dialog-content>
      <p>Are you sure you want to ban <b>{{ data.email }}</b>?</p>

      <mat-form-field appearance="fill" style="width:100%">
        <mat-label>Reason (shown to the user on login)</mat-label>
        <textarea matInput rows="4" [(ngModel)]="reason" placeholder="e.g. Chargeback abuse, repeated harassment..."></textarea>
      </mat-form-field>
    </div>

    <div mat-dialog-actions align="end">
      <button mat-button (click)="cancel()">Cancel</button>
      <button mat-raised-button color="warn" (click)="confirm()" [disabled]="!reason.trim()">
        Ban
      </button>
    </div>
  `
})
export class BanReasonDialog {
  reason = '';

  constructor(
    private ref: MatDialogRef<BanReasonDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { email: string }
  ) {}

  cancel() { this.ref.close(null); }
  confirm() { this.ref.close(this.reason.trim()); }
}