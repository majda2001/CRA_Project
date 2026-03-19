import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MissionRequest {
  titre: string;
  description: string;
  dateDebut: string;
  dateFin?: string | null;
  motif?: string;
  tjm: number;
}

export interface MissionResponse {
  id: number;
  titre: string;
  description: string;
  dateDebut: string;
  dateFin?: string | null;
  motif?: string;
  tjm: number;
}

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  private readonly apiUrl = `${environment.apiUrl}/admin/missions`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<MissionResponse[]> {
    return this.http.get<MissionResponse[]>(this.apiUrl);
  }

  create(payload: MissionRequest): Observable<MissionResponse> {
    return this.http.post<MissionResponse>(this.apiUrl, payload);
  }

  update(id: number, payload: MissionRequest): Observable<MissionResponse> {
    return this.http.put<MissionResponse>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
