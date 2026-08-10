import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { McpTool } from '../models/mcp-tool.model';

import { StorageService } from './storage.service';

@Injectable({
  providedIn: 'root'
})
export class McpToolService {
  private apiUrl = 'http://localhost:3001/tools';

  private toolsSubject = new BehaviorSubject<McpTool[]>([]);
  public tools$ = this.toolsSubject.asObservable();

  constructor(private http: HttpClient, private storageService: StorageService) { 
    this.refreshTools();
  }

  private getSessionId(): string {
    return this.storageService.getItem('agentSessionId') || '';
  }

  private currentMode: 'global' | 'session' = 'global';
  private currentSessionId: string = '';

  // 強制重新載入所有 Tools
  refreshTools(mode: 'global' | 'session' = 'global', sessionId: string = ''): void {
    this.currentMode = mode;
    this.currentSessionId = sessionId;
    
    let url = `${this.apiUrl}?mode=${mode}`;
    if (mode === 'session' && sessionId) {
      url += `&sessionId=${sessionId}`;
    }
    
    this.http.get<McpTool[]>(url).pipe(
      catchError(err => {
        console.error('Failed to fetch tools:', err);
        return throwError(() => err);
      })
    ).subscribe(tools => {
      this.toolsSubject.next(tools);
    });
  }

  // 新增 Tool
  addTool(tool: McpTool, mode: 'global' | 'session' = 'global'): Observable<McpTool> {
    if (mode === 'session') {
      if (this.currentSessionId) tool.session_id = this.currentSessionId;
    } else {
      delete tool.session_id; // Ensure global tools have no session_id
    }
    return this.http.post<McpTool>(this.apiUrl, tool).pipe(
      tap(() => this.refreshTools(this.currentMode, this.currentSessionId)), // 成功後重新整理清單
      catchError(err => {
        console.error('Failed to add tool:', err);
        return throwError(() => err);
      })
    );
  }

  // 更新 Tool
  updateTool(tool: McpTool): Observable<McpTool> {
    // 只有當 tool 已經是 session_id (或者建立時) 才會帶入，全域工具更新不應綁定到當前 Session
    return this.http.put<McpTool>(`${this.apiUrl}/${tool.id}`, tool).pipe(
      tap(() => this.refreshTools(this.currentMode, this.currentSessionId)),
      catchError(err => {
        console.error('Failed to update tool:', err);
        return throwError(() => err);
      })
    );
  }

  // 刪除 Tool
  deleteTool(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.refreshTools(this.currentMode, this.currentSessionId)),
      catchError(err => {
        console.error('Failed to delete tool:', err);
        return throwError(() => err);
      })
    );
  }
}
