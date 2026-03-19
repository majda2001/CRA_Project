import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AssignmentRequest {
  collaborateurId: number;
  missionId: number;
}

export interface AssignmentResponse {
  id: number;
  collaborateurId: number;
  collaborateurName: string;
  missionId: number;
  missionTitle: string;
  dateDebut: string;
  dateFin?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AssignmentService {
  private readonly apiUrl = `${environment.apiUrl}/admin/assignments`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<AssignmentResponse[]> {
    return this.http.get<AssignmentResponse[]>(this.apiUrl);
  }

  create(payload: AssignmentRequest): Observable<AssignmentResponse> {
    return this.http.post<AssignmentResponse>(this.apiUrl, payload);
  }

  update(id: number, payload: AssignmentRequest): Observable<AssignmentResponse> {
    return this.http.put<AssignmentResponse>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getByCollaborateur(userId: number): Observable<AssignmentResponse[]> {
    return this.http.get<AssignmentResponse[]>(`${this.apiUrl}/collaborateur/${userId}`);
  }
}
