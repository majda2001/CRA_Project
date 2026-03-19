import { Routes } from '@angular/router';
import { MainLayoutComponent } from './components/layout/main-layout.component';
import { adminGuard, authGuard, collaborateurGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'activate-account',
    loadComponent: () => import('./components/activate-account/activate-account.component').then((m) => m.ActivateAccountComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        canActivate: [adminGuard],
        loadComponent: () => import('./components/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'cra',
        canActivate: [collaborateurGuard],
        loadComponent: () => import('./components/cra/cra-calendar.component').then((m) => m.CraCalendarComponent)
      },
      {
        path: 'admin/collaborateurs',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./components/admin/collaborators/collaborators-list.component').then((m) => m.CollaboratorsListComponent)
      },
      {
        path: 'admin/missions',
        canActivate: [adminGuard],
        loadComponent: () => import('./components/admin/missions/missions-list.component').then((m) => m.MissionsListComponent)
      },
      {
        path: 'admin/assignments',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./components/admin/assignments/assignments-list.component').then((m) => m.AssignmentsListComponent)
      },
      {
        path: 'admin/cras',
        canActivate: [adminGuard],
        loadComponent: () => import('./components/admin/validation/cra-validation.component').then((m) => m.CraValidationComponent)
      }
    ]
  },
  { path: '**', redirectTo: '/login' }
];
