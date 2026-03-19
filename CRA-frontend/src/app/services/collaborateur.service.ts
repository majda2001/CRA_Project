import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CollaborateurRequest {
  firstName: string;
  lastName: string;
  email: string;
  statut: string;
  contrat: string;
  seniorite: string;
  salaire: number;
}

export interface CollaborateurResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ADMIN' | 'COLLABORATEUR';
  statut: string;
  contrat: string;
  seniorite: string;
  salaire: number;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CollaborateurService {
  private readonly apiUrl = `${environment.apiUrl}/admin/collaborateurs`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<CollaborateurResponse[]> {
    return this.http.get<CollaborateurResponse[]>(this.apiUrl);
  }

  create(payload: CollaborateurRequest): Observable<CollaborateurResponse> {
    return this.http.post<CollaborateurResponse>(this.apiUrl, payload);
  }

  update(id: number, payload: CollaborateurRequest): Observable<CollaborateurResponse> {
    return this.http.put<CollaborateurResponse>(`${this.apiUrl}/${id}`, payload);
  }

  toggleActivation(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/activate`, {});
  }
}
