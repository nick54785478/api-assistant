import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AvatarModule } from 'primeng/avatar';
import { AgentSessionService, AgentSession } from '../../core/services/agent-session.service';
import { StorageService } from '../../core/services/storage.service';
import { FormsModule } from '@angular/forms';
import { MessageService, MenuItem } from 'primeng/api';
import { MenuModule } from 'primeng/menu';

import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, AvatarModule, FormsModule, MenuModule, RouterModule],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit {
  @Input() name = '';
  @Input() username = 'Guest';
  @Output() openSettings = new EventEmitter<void>();
  @Output() openSessionSettings = new EventEmitter<string>();
  @Output() openNewChat = new EventEmitter<void>();
  @Output() openManagePlaybooks = new EventEmitter<string>();

  sessions: AgentSession[] = [];
  activeSessionId: string | null = null;
  editingSessionId: string | null = null;
  editNameValue: string = '';
  menuItems: MenuItem[] = [];

  constructor(
    private sessionService: AgentSessionService,
    private storageService: StorageService
  ) {}

  ngOnInit(): void {
    this.username = this.storageService.getItem('username') || 'Guest';
    this.activeSessionId = this.storageService.getItem('agentSessionId');
    this.loadSessions();
  }

  loadSessions() {
    this.sessionService.getSessions(this.username).subscribe({
      next: (data) => {
        this.sessions = data;
      },
      error: (err) => console.error('Failed to load sessions', err)
    });
  }

  openMenu(event: Event, menu: any, session: AgentSession) {
    event.stopPropagation();
    this.menuItems = [
      {
        label: '重新命名',
        icon: 'pi pi-pencil',
        command: () => {
          this.editingSessionId = session.sessionId;
          this.editNameValue = session.name || '';
        }
      },
      {
        label: '專屬 API 配置',
        icon: 'pi pi-box',
        command: () => {
          this.openSessionSettings.emit(session.sessionId);
        }
      },
      {
        label: '管理 Playbooks',
        icon: 'pi pi-book',
        command: () => {
          this.openManagePlaybooks.emit(session.sessionId);
        }
      }
    ];
    menu.toggle(event);
  }

  selectSession(sessionId: string) {
    this.activeSessionId = sessionId;
    if (sessionId) {
      this.storageService.setItem('agentSessionId', sessionId);
    } else {
      this.storageService.removeItem('agentSessionId');
    }
    // Ideally this would trigger a reload of messages in chat area
    // A full page reload is the easiest way to reset the state for now:
    window.location.href = '/';
  }

  startEditing(session: AgentSession, event: Event) {
    event.stopPropagation();
    this.editingSessionId = session.sessionId;
    this.editNameValue = session.name;
  }

  saveEdit(session: AgentSession, event: Event) {
    event.stopPropagation();
    if (this.editNameValue.trim() && this.editNameValue !== session.name) {
      this.sessionService.renameSession(session.sessionId, this.editNameValue).subscribe({
        next: (updated) => {
          session.name = updated.name;
          this.editingSessionId = null;
        },
        error: (err) => console.error('Failed to rename session', err)
      });
    } else {
      this.editingSessionId = null;
    }
  }

  cancelEdit(event: Event) {
    event.stopPropagation();
    this.editingSessionId = null;
  }
}
