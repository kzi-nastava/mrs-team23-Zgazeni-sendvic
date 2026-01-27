import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { interval, takeUntil, Subject } from 'rxjs';

@Component({
  selector: 'app-route-estimation-panel',
  imports: [CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './route-estimation-panel.html',
  styleUrl: './route-estimation-panel.css',
})
export class RouteEstimationPanel {
  countdownTime = signal<number>(0);
  isRunning = signal<boolean>(false);
  private stopTimer$ = new Subject<void>();

  estimateRoute() {
    if (this.isRunning()) {
      this.stopTimer$.next();
      this.isRunning.set(false);
      return;
    }

    this.countdownTime.set(300); // 5 minutes in seconds
    this.isRunning.set(true);

    interval(1000)
      .pipe(takeUntil(this.stopTimer$))
      .subscribe(() => {
        const current = this.countdownTime();
        if (current > 0) {
          this.countdownTime.set(current - 1);
        } else {
          this.isRunning.set(false);
          this.stopTimer$.next();
        }
      });
  }

  private formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  get displayTime(): string {
    return this.formatTime(this.countdownTime());
  }
}
