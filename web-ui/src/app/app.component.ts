import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { ButtonModule } from 'primeng/button';
import { SidebarModule } from 'primeng/sidebar';
import { InputTextModule } from 'primeng/inputtext';

// Services
import { ChatWebsocketService, ChatMessage } from './core/services/chat-websocket.service';
import { StorageService } from './core/services/storage.service';
import { LoadingService } from './core/services/loading.service';
import { AgentSessionService, PlaybookExecutionLog } from './core/services/agent-session.service';

// Components
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { ChatAreaComponent } from './features/chat/components/chat-area/chat-area.component';
import { McpToolManagerComponent } from './features/mcp-tools/components/mcp-tool-manager/mcp-tool-manager.component';
import { PlaybookListComponent } from './features/playbooks/playbook-list/playbook-list.component';
import { PlaybookCreateComponent } from './features/playbooks/playbook-create/playbook-create.component';

import { PlaybookRunsComponent } from './features/chat/components/playbook-runs/playbook-runs.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    SidebarComponent,
    ChatAreaComponent,
    McpToolManagerComponent,
    PlaybookListComponent,
    PlaybookRunsComponent,
    PlaybookCreateComponent,
    ProgressSpinnerModule,
    ToastModule,
    ConfirmDialogModule,
    DialogModule,
    DropdownModule,
    ButtonModule,
    SidebarModule,
    InputTextModule,
    FormsModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'API Assistant';
  userInput = '';
  username = 'Guest';
  name = '';
  isWaitingForResponse = false;
  showMcpManager = false;
  mcpMode: 'global' | 'session' = 'global';
  mcpSessionId: string = '';
  
  agentMode: string = 'GENERAL';
  isFastForwarding = false;
  playbookLogRuns: PlaybookExecutionLog[][] = [];
  
  messages: ChatMessage[] = [];
  private subscription!: Subscription;
  private agentModeSubscription!: Subscription;
  isChatRoute = true;
  
  // New Chat Dialog State
  showNewChatDialog = false;
  availablePlaybooks: any[] = [];
  selectedPlaybookId: string | null = null;
  newChatInitialMessage = '';
  
  // Playbooks state
  showPlaybooksDialog = false;
  playbookViewMode: 'list' | 'create' | 'edit' = 'list';
  showPlaybookRunsDialog = false;
  selectedPlaybookSessionId: string | undefined;
  editPlaybookId: string | undefined;
  
  private http = inject(HttpClient);

  constructor(
    private chatWsService: ChatWebsocketService,
    private storageService: StorageService,
    public loadingService: LoadingService,
    private agentSessionService: AgentSessionService,
    private router: Router
  ) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.isChatRoute = event.urlAfterRedirects === '/' || event.urlAfterRedirects.startsWith('/chat');
      }
    });
  }

  openGlobalSettings() {
    this.mcpMode = 'global';
    this.mcpSessionId = '';
    this.showMcpManager = true;
  }

  openSessionSettings(sessionId: string) {
    if (sessionId) {
      this.mcpMode = 'session';
      this.mcpSessionId = sessionId;
      this.showMcpManager = true;
    }
  }

  openManagePlaybooks(sessionId: string) {
    this.selectedPlaybookSessionId = sessionId;
    this.playbookViewMode = 'list';
    this.showPlaybooksDialog = true;
  }

  closePlaybooksDialog() {
    this.showPlaybooksDialog = false;
  }

  openCreatePlaybook() {
    setTimeout(() => {
      this.playbookViewMode = 'create';
      this.editPlaybookId = undefined;
    });
  }

  openEditPlaybook(playbookId: string) {
    setTimeout(() => {
      this.playbookViewMode = 'edit';
      this.editPlaybookId = playbookId;
    });
  }

  executePlaybook(playbookId: string) {
    this.showPlaybooksDialog = false;
    
    // Send a message to execute the playbook
    const initialMessage = "請執行這個劇本";
    this.messages.push({ role: 'user', content: initialMessage });
    this.isWaitingForResponse = true;
    
    this.chatWsService.sendMessage(initialMessage, playbookId);
    
    // Ensure we navigate to the chat route if we aren't there already
    if (!this.isChatRoute) {
      this.router.navigate(['/']);
    }
  }

  openPlaybookRunsDialog(sessionId: string) {
    this.selectedPlaybookSessionId = sessionId;
    this.showPlaybookRunsDialog = true;
  }

  onPlaybookSaved() {
    setTimeout(() => {
      this.playbookViewMode = 'list';
    });
  }

  onPlaybookCanceled() {
    setTimeout(() => {
      this.playbookViewMode = 'list';
    });
  }
  
  fetchPlaybookLogs() {
    const sessionId = this.storageService.getItem('agentSessionId');
    if (sessionId && this.agentMode === 'PLAYBOOK') {
      this.agentSessionService.getPlaybookLogs(sessionId).subscribe({
        next: (logs) => {
          const runs: PlaybookExecutionLog[][] = [];
          let currentRun: PlaybookExecutionLog[] = [];
          for (const log of logs) {
            if (log.stepIndex === 0 && currentRun.length > 0) {
              runs.push([...currentRun]);
              currentRun = [];
            }
            currentRun.push(log);
          }
          if (currentRun.length > 0) {
            runs.push(currentRun);
          }
          // Limit to the last 10 runs to prevent UI performance issues
          this.playbookLogRuns = runs.slice(-10);
        },
        error: (err) => console.error('Failed to load playbook logs', err)
      });
    }
  }

  ngOnInit() {
    this.username = this.storageService.getItem('username') || 'Guest';
    this.name = this.storageService.getItem('name') || '';

    const sessionId = this.storageService.getItem('agentSessionId');
    if (sessionId) {
      this.agentSessionService.getChatHistory(sessionId).subscribe({
        next: (history) => {
          this.messages = history.map(msg => ({
            role: (msg.role === 'assistant' || msg.role === 'ai') ? 'ai' : 'user',
            content: msg.content
          }));
        },
        error: (err) => console.error('Failed to load chat history', err)
      });
    }

    // Subscribe to incoming messages from the backend
    this.subscription = this.chatWsService.messages$.subscribe(msg => {
      this.isWaitingForResponse = false;
      
      if (msg.isDelta) {
        const lastMsg = this.messages.length > 0 ? this.messages[this.messages.length - 1] : null;
        if (lastMsg && lastMsg.role === 'ai' && (lastMsg as any)._isStreaming) {
          lastMsg.content += msg.content;
        } else {
          this.messages.push({ role: msg.role, content: msg.content, _isStreaming: true } as any);
        }
      } else if (msg.isFinal) {
        const lastMsg = this.messages.length > 0 ? this.messages[this.messages.length - 1] : null;
        if (lastMsg && lastMsg.role === 'ai' && (lastMsg as any)._isStreaming) {
          lastMsg.content += msg.content;
          (lastMsg as any)._isStreaming = false;
        } else if (msg.content) {
          this.messages.push(msg);
        }

        // Handle auto-continue for fast forwarding
        if (this.isFastForwarding && this.agentMode === 'PLAYBOOK') {
          setTimeout(() => {
            if (this.isFastForwarding && !this.isWaitingForResponse && this.agentMode === 'PLAYBOOK') {
              this.autoContinue();
            }
          }, 1000); // 1s delay for better UX and readability
        }
        
        if (this.agentMode === 'PLAYBOOK') {
          this.fetchPlaybookLogs();
        }
      } else {
        this.messages.push(msg);
      }
    });

    // Subscribe to agent mode changes
    this.agentModeSubscription = this.chatWsService.agentMode$.subscribe(mode => {
      this.agentMode = mode;
      if (mode !== 'PLAYBOOK') {
        this.isFastForwarding = false;
      } else {
        this.fetchPlaybookLogs();
      }
    });
  }

  sendMessage() {
    if (!this.userInput.trim()) return;
    
    // 1. Add user message to UI immediately
    this.messages.push({ role: 'user', content: this.userInput });
    this.isWaitingForResponse = true;
    
    // 2. Send via WebSocket to Backend
    this.chatWsService.sendMessage(this.userInput);
    
    // 3. Clear input
    this.userInput = '';
  }

  toggleFastForward() {
    this.isFastForwarding = !this.isFastForwarding;
    if (this.isFastForwarding && !this.isWaitingForResponse && this.agentMode === 'PLAYBOOK') {
      this.autoContinue();
    }
  }

  autoContinue() {
    this.userInput = '繼續';
    this.sendMessage();
  }
  
  openNewChatDialog() {
    this.http.get<any[]>('/api/v1/playbooks').subscribe({
      next: (data) => this.availablePlaybooks = data,
      error: (err) => console.error('Failed to load playbooks', err)
    });
    this.newChatInitialMessage = '';
    this.selectedPlaybookId = null;
    this.showNewChatDialog = true;
  }
  
  startNewChat() {
    if (!this.newChatInitialMessage.trim()) return;
    
    this.storageService.removeItem('agentSessionId');
    this.messages = [{ role: 'user', content: this.newChatInitialMessage }];
    this.isWaitingForResponse = true;
    
    this.chatWsService.sendMessage(this.newChatInitialMessage, this.selectedPlaybookId || undefined);
    
    this.showNewChatDialog = false;
    this.router.navigate(['/']);
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.agentModeSubscription) {
      this.agentModeSubscription.unsubscribe();
    }
  }
}
