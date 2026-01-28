import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-confirm-account',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="confirm-container">
      <p>Confirming your account...</p>
    </div>
  `,
  styles: [`
    .confirm-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 24px;
      font-size: 18px;
    }
  `]
})
export class ConfirmAccount implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      
      if (!token) {
        console.warn('No token provided');
        this.router.navigate(['/login']);
        return;
      }

      this.confirmAccount(token);
    });
  }

  private confirmAccount(token: string) {
    this.authService.confirmAccount(token).subscribe({
      next: (response) => {
        console.log('Account confirmed successfully', response);
        this.router.navigate(['/login']);
      },
      error: (error) => {
        // Redirect to login regardless of success/failure
        console.log('Account confirmation completed (redirecting to login)', error);
        this.router.navigate(['/login']);
      }
    });
  }
}
