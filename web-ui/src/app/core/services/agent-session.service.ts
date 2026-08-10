import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AgentSession {
  sessionId: string;
  name: string;
  status: string;
  createdAt: string;
}

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
}
