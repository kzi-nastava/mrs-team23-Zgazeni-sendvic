import { Injectable } from '@angular/core';
import { switchMap, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { RouteEstimationService, RouteResult } from './route.estimation.service';

@Injectable({ providedIn: 'root' })
export class RouteEstimationFacade {

  constructor(private routeService: RouteEstimationService) {}

  estimateRoute(startText: string, endText: string): Observable<RouteResult> {
    if (!startText || !endText) {
      return throwError(() => new Error('Missing start or end'));
    }

    return this.routeService.geocode(startText).pipe(
      switchMap(start => {
        if (!start) {
          return throwError(() => new Error('Start not found'));
        }

        return this.routeService.geocode(endText).pipe(
          switchMap(end => {
            if (!end) {
              return throwError(() => new Error('End not found'));
            }

            return this.routeService.getRoute([start, end]);
          })
        );
      })
    );
  }

  estimateRouteByCoords(points: any[]): Observable<RouteResult> {
    return this.routeService.getRouteMulti(points);
  }
}