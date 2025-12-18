import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterModule, RouterLinkWithHref } from '@angular/router';
import { NavBar } from './layout/nav-bar/nav-bar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLinkWithHref, RouterModule, NavBar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Web-Front-IKS');
}
