import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../services/auth.service';
import { PanelService } from '../../service/panel.service';

@Component({
  selector: 'app-nav-bar',
  imports: [RouterModule, CommonModule, MatButtonModule],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBar {
  constructor(private authService: AuthService, private router: Router, public panelService: PanelService) {}

  logout() {
    this.authService.clearToken();
    this.router.navigate(['/login']);
  }

  isHomePage(): boolean {
    return this.router.url === '/';
  }
}
