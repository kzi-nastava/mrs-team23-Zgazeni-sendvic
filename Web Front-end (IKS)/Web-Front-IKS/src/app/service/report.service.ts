import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly api = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  getRideReport(from: string, to: string, scope: string, accountId?: number) {
    let params = new HttpParams()
      .set('from', from)
      .set('to', to)
      .set('scope', scope);

    if (accountId != null) params = params.set('accountId', accountId);

    return this.http.get<any>(`${this.api}/rides`, { params });
  }
}