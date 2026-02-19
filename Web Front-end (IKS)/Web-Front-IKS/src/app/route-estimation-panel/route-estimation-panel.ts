import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatOptionModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { RouteEstimationService, RouteResult } from '../service/route.estimation.serivce';
import { switchMap } from 'rxjs/operators';


@Component({
  selector: 'app-route-estimation-panel',
  imports: [
    CommonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatOptionModule,
    MatButtonModule,
    MatIconModule,
    FormsModule,
  ],
  templateUrl: './route-estimation-panel.html',
  styleUrl: './route-estimation-panel.css',
})
export class RouteEstimationPanel {
  estimatedTime = signal<number | null>(null);
  panelVisible = signal<boolean>(true);
  beginningDestination = signal<string>('');
  endingDestination = signal<string>('');
  errorMessage = signal<string | null>(null);

  beginningSuggestions: NominatimSuggestion[] = [];
  endingSuggestions: NominatimSuggestion[] = [];

  private readonly suggestionDelayMs = 350;
  private beginningTimeoutId: number | null = null;
  private endingTimeoutId: number | null = null;
  private lastBeginningQueryId = 0;
  private lastEndingQueryId = 0;
  private readonly noviSadBounds = {
    minLon: 19.70,
    minLat: 45.18,
    maxLon: 19.98,
    maxLat: 45.36,
  };

  constructor(
    private routeEstimationService: RouteEstimationService,
    private http: HttpClient
  ) {}

  onBeginningInput(value: string) {
    this.beginningDestination.set(value);
    this.scheduleSuggestions(value, 'beginning');
  }

  onEndingInput(value: string) {
    this.endingDestination.set(value);
    this.scheduleSuggestions(value, 'ending');
  }

  onBeginningOptionSelected(value: string) {
    this.beginningDestination.set(value);
    this.beginningSuggestions = [];
  }

  onEndingOptionSelected(value: string) {
    this.endingDestination.set(value);
    this.endingSuggestions = [];
  }

  estimateRoute() {
    this.estimatedTime.set(null);
    this.routeEstimationService.setRoutePath(null);
    const beginning = this.beginningDestination();
    const ending = this.endingDestination();

    if (!beginning || !ending) {
      this.errorMessage.set('Both destinations must be chosen.');
      return;
    }

    this.errorMessage.set(null);

    this.routeEstimationService.geocode(beginning)
      .pipe(
        switchMap((start) => {
          if (!start) {
            throw new Error('Start not found');
          }
          return this.routeEstimationService.geocode(ending).pipe(
            switchMap((end) => {
              if (!end) {
                throw new Error('End not found');
              }
              return this.routeEstimationService.getRoute(start, end);
            })
          );
        })
      )
      .subscribe({
        next: (route: RouteResult) => {
          this.estimatedTime.set(Math.ceil(route.durationSeconds / 60));
          this.routeEstimationService.setRoutePath(route.pathCoordinates ?? null);
          this.errorMessage.set(null);
        },
        error: (error) => {
          console.error('Error estimating route:', error);
          this.errorMessage.set('Please enter appropriate destinations.');
          this.estimatedTime.set(null);
          this.routeEstimationService.setRoutePath(null);
        }
      });
  }

  private scheduleSuggestions(query: string, type: 'beginning' | 'ending') {
    const trimmed = query.trim();
    if (type === 'beginning' && this.beginningTimeoutId) {
      clearTimeout(this.beginningTimeoutId);
    }
    if (type === 'ending' && this.endingTimeoutId) {
      clearTimeout(this.endingTimeoutId);
    }

    if (trimmed.length < 3) {
      if (type === 'beginning') {
        this.beginningSuggestions = [];
      } else {
        this.endingSuggestions = [];
      }
      return;
    }

    const timeoutId = window.setTimeout(() => {
      this.loadSuggestions(trimmed, type);
    }, this.suggestionDelayMs);

    if (type === 'beginning') {
      this.beginningTimeoutId = timeoutId;
    } else {
      this.endingTimeoutId = timeoutId;
    }
  }

  private loadSuggestions(query: string, type: 'beginning' | 'ending') {
    const queryId = type === 'beginning' ? ++this.lastBeginningQueryId : ++this.lastEndingQueryId;
    const { minLon, minLat, maxLon, maxLat } = this.noviSadBounds;
    const url = `https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(query)}&limit=6&viewbox=${minLon},${maxLat},${maxLon},${minLat}&bounded=1&countrycodes=rs&addressdetails=1`;

    this.http.get<NominatimSuggestion[]>(url)
      .pipe(catchError(() => of([])))
      .subscribe((results) => {
        if (type === 'beginning' && queryId === this.lastBeginningQueryId) {
          this.beginningSuggestions = results;
        }
        if (type === 'ending' && queryId === this.lastEndingQueryId) {
          this.endingSuggestions = results;
        }
      });
  }

  togglePanelVisibility() {
    this.panelVisible.set(!this.panelVisible());
  }

  onCancel() {
    console.log('Cancel clicked');
    // Handle cancel action
  }

  onStop() {
    console.log('Stop clicked');
    // Handle stop action
  }

  onPanic() {
    console.log('PANIC clicked');
    // Handle panic action
  }
}

interface NominatimSuggestion {
  place_id: number;
  display_name: string;
  lat: string;
  lon: string;
}
