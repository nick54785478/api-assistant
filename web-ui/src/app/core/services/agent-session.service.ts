import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AgentSession, PlaybookExecutionLog, PlaybookRun, Page } from '../models/agent-session.model';
@Injectable({
  providedIn: 'root'
})
export class AgentSessionService {
  
  private apiUrl = '/api/v1/agent-sessions';

  constructor(private http: HttpClient) {}

  getSessions(username: string): Observable<AgentSession[]> {
    let params = new HttpParams().set('username', username);
    return this.http.get<AgentSession[]>(this.apiUrl, { params });
  }

  renameSession(sessionId: string, newName: string): Observable<AgentSession> {
    return this.http.patch<AgentSession>(`${this.apiUrl}/${sessionId}/name`, { name: newName });
  }

  getChatHistory(sessionId: string): Observable<{role: string, content: string}[]> {
    return this.http.get<{role: string, content: string}[]>(`${this.apiUrl}/${sessionId}/messages`);
  }

  getPlaybookLogs(sessionId: string): Observable<PlaybookExecutionLog[]> {
    return this.http.get<PlaybookExecutionLog[]>(`${this.apiUrl}/${sessionId}/playbook-logs`);
  }

  getPlaybookRuns(sessionId: string, page: number = 0, size: number = 10): Observable<Page<PlaybookRun>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<PlaybookRun>>(`${this.apiUrl}/${sessionId}/playbook-runs`, { params });
  }
}
