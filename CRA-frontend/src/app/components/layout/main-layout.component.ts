import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavigationEnd, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { animate, query, style, transition, trigger } from '@angular/animations';
import { AuthService } from '../../services/auth.service';
import { map, Observable } from 'rxjs';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule],
  animations: [
    trigger('routeFade', [
      transition('* => *', [
        query(
          ':enter',
          [
            style({ opacity: 0, transform: 'translateY(8px)' }),
            animate('240ms cubic-bezier(0.21, 1, 0.35, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
          ],
          { optional: true }
        )
      ])
    ])
  ],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent {
  isAdmin$: Observable<boolean>;
  isCollaborateur$: Observable<boolean>;
  isDarkMode = false;
  routeAnimationState = 0;

  constructor(private authService: AuthService, private router: Router) {
    this.isAdmin$ = this.authService.currentUser$.pipe(map((user) => user?.role === 'ADMIN'));
    this.isCollaborateur$ = this.authService.currentUser$.pipe(map((user) => user?.role === 'COLLABORATEUR'));

    this.initializeTheme();
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.routeAnimationState++;
      }
    });
  }

  private initializeTheme(): void {
    const stored = localStorage.getItem('theme');
    this.isDarkMode = stored === 'dark';
    document.documentElement.setAttribute('data-theme', this.isDarkMode ? 'dark' : 'light');
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    const theme = this.isDarkMode ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }

  logout(): void {
    this.authService.logout();
  }
}
