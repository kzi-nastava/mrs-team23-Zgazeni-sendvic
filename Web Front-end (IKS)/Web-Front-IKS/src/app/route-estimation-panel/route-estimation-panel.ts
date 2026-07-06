import { Component, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { MatOptionModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { RouteEstimationFacade } from '../service/route-estimation.facade';
import { RouteResult, RouteEstimationService } from '../service/route.estimation.service';
import { switchMap } from 'rxjs/operators';

export interface NominatimSuggestion {
  place_id: number;
  display_name: string;
  lat: string;
  lon: string;
}

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
  @ViewChild('startTrigger') startTrigger?: MatAutocompleteTrigger;
  @ViewChild('endTrigger') endTrigger?: MatAutocompleteTrigger;

  estimatedTime = signal<number | null>(null);
  panelVisible = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  beginningLocation: RoutePoint | null = null;
  endingLocation: RoutePoint | null = null;

  beginningSuggestions: NominatimSuggestion[] = [];
  endingSuggestions: NominatimSuggestion[] = [];

  private readonly suggestionDelayMs = 350;
  private beginningTimeoutId: number | null = null;
  private endingTimeoutId: number | null = null;
  private lastBeginningQueryId = 0;
  private lastEndingQueryId = 0;

  private activeField: 'beginning' | 'ending' | null = null;

  constructor(
    private routeFacade: RouteEstimationFacade,
    private routeEstimationService: RouteEstimationService,
    private http: HttpClient
  ) {}

  onBeginningInput(value: Event) {
    this.scheduleSuggestions((value.target as HTMLInputElement).value, 'beginning');
  }

  onEndingInput(value: Event) {
    this.scheduleSuggestions((value.target as HTMLInputElement).value, 'ending');
  }

  onBeginningOptionSelected(s: NominatimSuggestion) {
    this.beginningLocation = {
      lat: Number(s.lat),
      lon: Number(s.lon),
      label: s.display_name
    };
    this.beginningSuggestions = [];
  }

  onEndingOptionSelected(s: NominatimSuggestion) {
    this.endingLocation = {
      lat: Number(s.lat),
      lon: Number(s.lon),
      label: s.display_name
    };
    this.endingSuggestions = [];
  }

  estimateRoute() {
    if (!this.beginningLocation || !this.endingLocation) {
      this.errorMessage.set('Both locations must be selected.');
      return;
    }

    const points = [
      this.beginningLocation,
      this.endingLocation
    ];

    this.routeFacade.estimateRouteByCoords(points)
      .subscribe({
        next: (route) => {

          // convert seconds → minutes
          this.estimatedTime.set(Math.ceil(route.durationSeconds / 60));

          // push full polyline
          this.routeEstimationService.setRoutePath(route.coordinates);
        },
        error: () => {
          this.errorMessage.set('Route calculation failed.');
        }
      });
  }

  private scheduleSuggestions(query: string, type: 'beginning' | 'ending') {
    const trimmed = query.trim();

    if (trimmed.length < 3) {
      if (type === 'beginning') this.beginningSuggestions = [];
      else this.endingSuggestions = [];
      return;
    }

    const timeoutId = window.setTimeout(() => {
      this.loadSuggestions(trimmed, type);
    }, this.suggestionDelayMs);

    if (type === 'beginning') this.beginningTimeoutId = timeoutId;
    else this.endingTimeoutId = timeoutId;
  }

  private loadSuggestions(query: string, type: 'beginning' | 'ending') {
    const queryId =
      type === 'beginning'
        ? ++this.lastBeginningQueryId
        : ++this.lastEndingQueryId;

    const url =
      `https://nominatim.openstreetmap.org/search?format=jsonv2&q=${encodeURIComponent(query)}&limit=6`;

    this.http.get<NominatimSuggestion[]>(url)
      .pipe(catchError(() => of([])))
      .subscribe(results => {
        if (type === 'beginning' && queryId === this.lastBeginningQueryId) {
          this.beginningSuggestions = results;
        }

        if (type === 'ending' && queryId === this.lastEndingQueryId) {
          this.endingSuggestions = results;
        }
      });
  }

  togglePanel(trigger: MatAutocompleteTrigger | undefined, open: boolean) {
    if (!trigger) return;
    open ? trigger.openPanel() : trigger.closePanel();
  }

  togglePanelVisibility() {
    this.panelVisible.set(!this.panelVisible());
  }

  onCancel() {}
  onStop() {}
  onPanic() {}
}

interface RoutePoint {
  lat: number;
  lon: number;
  label: string;
}
