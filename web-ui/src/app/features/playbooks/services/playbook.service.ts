import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Playbook, CreatePlaybookRequest } from '../models/playbook.model';

@Injectable({
  providedIn: 'root'
})
export class PlaybookService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/playbooks';

  getPlaybooks(): Observable<Playbook[]> {
    return this.http.get<Playbook[]>(this.apiUrl);
  }

  getPlaybook(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createPlaybook(playbook: any): Observable<any> {
    return this.http.post(this.apiUrl, playbook);
  }

  updatePlaybook(id: string, playbook: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, playbook);
  }

  clonePlaybook(id: string, targetAgentSessionId?: string): Observable<any> {
    let url = `${this.apiUrl}/${id}/clone`;
    if (targetAgentSessionId) {
      url += `?targetAgentSessionId=${targetAgentSessionId}`;
    }
    return this.http.post(url, {});
  }
}
