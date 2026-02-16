import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-driver-accept-edit',
  standalone: true,
  templateUrl: './driver-accept-edit.html',
  styleUrls: ['./driver-accept-edit.css'],
  imports: [CommonModule]
})
export class DriverAcceptEdit implements OnInit {

  message = 'Processing...';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {

    const id = this.route.snapshot.queryParamMap.get('id');

    if (!id) {
      this.message = "Invalid request.";
      return;
    }

    this.http.put(
      `http://localhost:8080/api/account/approve-driver-changes/${id}`,
      {},
      { responseType: 'text' }
    ).subscribe({
      next: (res) => this.message = res,
      error: (err) => {
        if (err.status === 403) {
          this.message = "Not authorized.";
        } else {
          this.message = "Error approving changes.";
        }
      }
    });
  }
}
