import { Component, EventEmitter, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, map, switchMap } from 'rxjs/operators';


@Component({
  selector: 'app-route-estimation-panel',
  imports: [CommonModule, MatCardModule, MatFormFieldModule, MatInputModule, MatAutocompleteModule, MatButtonModule, MatIconModule, FormsModule],
  templateUrl: './route-estimation-panel.html',
  styleUrl: './route-estimation-panel.css',
})
export class RouteEstimationPanel {
  @Output() routeRequested = new EventEmitter<{ start: string; end: string }>();
  estimatedTime = signal<number | null>(null);
  panelVisible = signal<boolean>(true);
  beginningDestination = signal<string>('');
  endingDestination = signal<string>('');
  errorMessage = signal<string | null>(null);
  startSuggestions = signal<string[]>([]);
  endSuggestions = signal<string[]>([]);
  private startQuery$ = new Subject<string>();
  private endQuery$ = new Subject<string>();

  constructor(
    private http: HttpClient
  ) {
    this.startQuery$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) => this.fetchSuggestions(query))
      )
      .subscribe((results) => this.startSuggestions.set(results));

    this.endQuery$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) => this.fetchSuggestions(query))
      )
      .subscribe((results) => this.endSuggestions.set(results));
  }

  estimateRoute() {
    this.estimatedTime.set(null);
    const beginning = this.beginningDestination();
    const ending = this.endingDestination();

    if (!beginning || !ending) {
      this.errorMessage.set('Both destinations must be chosen.');
      return;
    }

    this.errorMessage.set(null);

    this.routeRequested.emit({ start: beginning, end: ending });
  }

  onStartInput(value: string) {
    const query = value?.trim() ?? '';
    if (query.length < 3) {
      this.startSuggestions.set([]);
      return;
    }

    this.startQuery$.next(query);
  }

  onEndInput(value: string) {
    const query = value?.trim() ?? '';
    if (query.length < 3) {
      this.endSuggestions.set([]);
      return;
    }

    this.endQuery$.next(query);
  }

  onStartSelected(value: string) {
    this.beginningDestination.set(value);
    this.startSuggestions.set([]);
  }

  onEndSelected(value: string) {
    this.endingDestination.set(value);
    this.endSuggestions.set([]);
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

  private fetchSuggestions(query: string) {
    const url = `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&q=${encodeURIComponent(query)}`;
    return this.http.get<Array<{ display_name?: string }>>(url).pipe(
      map((results) =>
        (results ?? [])
          .map((item) => item.display_name)
          .filter((name): name is string => Boolean(name))
      ),
      catchError(() => of([]))
    );
  }
}
