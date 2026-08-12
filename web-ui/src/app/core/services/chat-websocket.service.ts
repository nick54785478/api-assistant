import { Injectable, OnDestroy } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { ReplaySubject, Observable, BehaviorSubject } from 'rxjs';
import { StorageService } from './storage.service';

import { ChatMessage } from '../models/chat-message.model';
@Injectable({
  providedIn: 'root'
})
export class ChatWebsocketService implements OnDestroy {
  private socket$!: WebSocketSubject<any>;
  private messagesSubject = new ReplaySubject<ChatMessage>(100);
  public messages$ = this.messagesSubject.asObservable();
  
  private agentModeSubject = new BehaviorSubject<string>('GENERAL');
  public agentMode$ = this.agentModeSubject.asObservable();

  constructor(private storageService: StorageService) {
    this.connect();
  }

  private connect(): void {
    // Connect through Angular Proxy
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    this.socket$ = webSocket(`${protocol}//${window.location.host}/ws/chat`);

    this.socket$.subscribe({
      next: (msg: any) => {
        if (msg && msg.sessionId) {
          // Save session ID if provided by backend
          this.storageService.setItem('agentSessionId', msg.sessionId);
        }
        if (msg && msg.role && msg.content !== undefined) {
          if (msg.agentMode) {
            this.agentModeSubject.next(msg.agentMode);
          }
          this.messagesSubject.next({ 
            role: msg.role, 
            content: msg.content,
            isDelta: msg.isDelta,
            isFinal: msg.isFinal,
            agentMode: msg.agentMode
          });
        }
      },
      error: (err) => console.error('WebSocket Error:', err),
      complete: () => console.warn('WebSocket connection closed')
    });
  }

  public sendMessage(content: string, playbookId?: string): void {
    if (this.socket$ && !this.socket$.closed) {
      const sessionId = this.storageService.getItem('agentSessionId') || null;
      const username = this.storageService.getItem('username') || 'Guest';
      this.socket$.next({
        sessionId: sessionId,
        username: username,
        content: content,
        playbookId: playbookId || null
      });
    }
  }

  ngOnDestroy(): void {
    if (this.socket$) {
      this.socket$.complete();
    }
  }
}
