import { Component, OnInit, inject, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { PlaybookService } from '../services/playbook.service';
import { Playbook } from '../models/playbook.model';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-playbook-list',
  standalone: true,
  imports: [CommonModule, ButtonModule, CardModule],
  templateUrl: './playbook-list.component.html',
  styleUrls: ['./playbook-list.component.scss']
})
export class PlaybookListComponent implements OnInit {
  @Input() agentSessionId?: string;
  @Output() onCreatePlaybook = new EventEmitter<void>();
  @Output() onEditPlaybook = new EventEmitter<string>();
  @Output() onExecutePlaybook = new EventEmitter<string>();

  private playbookService = inject(PlaybookService);
  private messageService = inject(MessageService);

  playbooks: Playbook[] = [];

  ngOnInit() {
    this.playbookService.getPlaybooks().subscribe({
      next: (data) => {
        let playbooksToShow = data;
        
        if (this.agentSessionId) {
          // Include playbooks that belong to the session OR are orphans (no session ID)
          playbooksToShow = data.filter((p: Playbook) => p.agentSessionId === this.agentSessionId || !p.agentSessionId);
        }

        // Sort to ensure orphan (reference) playbooks are at the top
        this.playbooks = playbooksToShow.sort((a: Playbook, b: Playbook) => {
          if (!a.agentSessionId && b.agentSessionId) return -1;
          if (a.agentSessionId && !b.agentSessionId) return 1;
          return 0;
        });
      },
      error: (err) => console.error('Error fetching playbooks', err)
    });
  }

  createNew(event?: Event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    this.onCreatePlaybook.emit();
  }

  editPlaybook(id: string, event?: Event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    this.onEditPlaybook.emit(id);
  }

  executePlaybook(id: string, event: Event) {
    event.stopPropagation();
    this.onExecutePlaybook.emit(id);
  }

  clonePlaybook(id: string, event: Event) {
    event.stopPropagation();
    // If the list is opened in the context of an agentSessionId, clone it to that session.
    // Otherwise, clone it as another reference (orphan) playbook.
    this.playbookService.clonePlaybook(id, this.agentSessionId).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Playbook cloned successfully!' });
        this.ngOnInit(); // Refresh list
      },
      error: (err) => {
        console.error('Error cloning playbook', err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to clone playbook.' });
      }
    });
  }
}
