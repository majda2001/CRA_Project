import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'COLLABORATEUR';
  accessToken: string;
  refreshToken?: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  role: 'ADMIN' | 'COLLABORATEUR';
}

interface JwtPayload {
  exp?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly currentUserSubject: BehaviorSubject<User | null>;
  readonly currentUser$: Observable<User | null>;

  constructor(private readonly http: HttpClient, private readonly router: Router) {
    const stored = localStorage.getItem('currentUser');
    const parsed = stored ? (JSON.parse(stored) as User) : null;

    if (parsed && !this.isTokenValid(parsed.accessToken)) {
      localStorage.removeItem('currentUser');
      this.currentUserSubject = new BehaviorSubject<User | null>(null);
    } else {
      this.currentUserSubject = new BehaviorSubject<User | null>(parsed);
    }

    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  getUser(): User | null {
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return this.currentUserSubject.value?.accessToken || null;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && this.isTokenValid(token);
  }

  hasRole(role: 'ADMIN' | 'COLLABORATEUR'): boolean {
    return this.currentUserSubject.value?.role === role;
  }

  activate(token: string): Observable<{ message: string }> {
    return this.http.get<{ message: string }>(`${this.apiUrl}/activate?token=${encodeURIComponent(token)}`);
  }

  completeInvitation(token: string, password: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/invitation/complete?token=${encodeURIComponent(token)}`, {
      password
    });
  }

  login(credentials: { email: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        const user: User = {
          id: 0,
          email: response.email,
          firstName: '',
          lastName: '',
          role: response.role,
          accessToken: response.accessToken,
          refreshToken: response.refreshToken
        };

        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  private isTokenValid(token: string): boolean {
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      if (!decoded.exp) {
        return false;
      }
      const now = Math.floor(Date.now() / 1000);
      return decoded.exp > now;
    } catch {
      return false;
    }
  }
}
