import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CraDto, CraStatus, CraConfiguration } from '../models/cra.model';

@Injectable({
  providedIn: 'root'
})
export class CraService {
  private readonly apiUrl = `${environment.apiUrl}`;

  constructor(private readonly http: HttpClient) {}

  getConfig(): Observable<CraConfiguration> {
    return this.http.get<CraConfiguration>(`${this.apiUrl}/cra/config`);
  }

  getCurrent(mois: number, annee: number): Observable<CraDto> {
    const params = new HttpParams().set('mois', mois).set('annee', annee);
    return this.http.get<CraDto>(`${this.apiUrl}/cra`, { params });
  }

  updateActivities(
    craId: number,
    payload: Array<{ date: string; duree: number; type: string; missionId: number | null }>
  ): Observable<CraDto> {
    return this.http.put<CraDto>(`${this.apiUrl}/cra/${craId}/activities`, payload);
  }

  submit(craId: number): Observable<CraDto> {
    return this.http.post<CraDto>(`${this.apiUrl}/cra/${craId}/submit`, {});
  }

  getAll(mois?: number, annee?: number, status?: CraStatus): Observable<CraDto[]> {
    let params = new HttpParams();
    if (mois != null) {
      params = params.set('mois', mois);
    }
    if (annee != null) {
      params = params.set('annee', annee);
    }
    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<CraDto[]>(`${this.apiUrl}/admin/cras`, { params });
  }

  validate(id: number): Observable<CraDto> {
    return this.http.put<CraDto>(`${this.apiUrl}/admin/cras/${id}/validate`, {});
  }

  reject(id: number, motif: string): Observable<CraDto> {
    return this.http.put<CraDto>(`${this.apiUrl}/admin/cras/${id}/reject`, { motif });
  }

  invalidate(id: number, motif: string): Observable<CraDto> {
    return this.http.put<CraDto>(`${this.apiUrl}/admin/cras/${id}/invalidate`, { motif });
  }

  exportCsv(mois?: number, annee?: number): Observable<string> {
    let params = new HttpParams();
    if (mois != null) {
      params = params.set('mois', mois);
    }
    if (annee != null) {
      params = params.set('annee', annee);
    }

    return this.http.get(`${this.apiUrl}/admin/cras/export`, {
      params,
      responseType: 'text'
    });
  }
}
