import { Component, signal, OnInit, inject } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { NavBar } from './layout/nav-bar/nav-bar';
import { AuthService } from './services/auth.service';
import { PanicNotificationsService } from './service/panic-notifications.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterModule, NavBar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('Web-Front-IKS');
  
  private authService = inject(AuthService);
  private panicService = inject(PanicNotificationsService);
  
  ngOnInit(): void {
    // Initialize global panic notifications if user is admin
    const userRole = this.authService.getRole();
    if (userRole === 'ADMIN') {
      this.panicService.initializeGlobalNotifications();
    }
  }
}
