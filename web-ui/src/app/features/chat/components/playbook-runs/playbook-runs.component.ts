import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TimelineModule } from 'primeng/timeline';
import { CardModule } from 'primeng/card';
import { AgentSessionService } from '../../../../core/services/agent-session.service';
import { PlaybookRun } from '../../../../core/models/agent-session.model';
import { MarkdownModule } from 'ngx-markdown';

@Component({
  selector: 'app-playbook-runs',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, TagModule, TimelineModule, CardModule, MarkdownModule],
  templateUrl: './playbook-runs.component.html',
  styleUrl: './playbook-runs.component.scss'
})
export class PlaybookRunsComponent implements OnInit, OnChanges {
  @Input() agentSessionId?: string;

  runs: PlaybookRun[] = [];
  totalRecords = 0;
  loading = false;

  constructor(private sessionService: AgentSessionService) {}

  ngOnInit() {
    if (this.agentSessionId) {
      this.loadRuns({ first: 0, rows: 10 });
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['agentSessionId'] && !changes['agentSessionId'].firstChange) {
      this.loadRuns({ first: 0, rows: 10 });
    }
  }

  loadRuns(event: any) {
    if (!this.agentSessionId) return;

    this.loading = true;
    const page = event.first / event.rows;
    const size = event.rows;

    this.sessionService.getPlaybookRuns(this.agentSessionId, page, size).subscribe({
      next: (data) => {
        this.runs = data.content;
        this.totalRecords = data.totalElements;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load playbook runs', err);
        this.loading = false;
      }
    });
  }

  getSeverity(status: string) {
    switch (status) {
      case 'SUCCESS':
      case 'COMPLETED':
        return 'success';
      case 'FAILED':
        return 'danger';
      case 'IN_PROGRESS':
        return 'info';
      default:
        return 'info';
    }
  }
}
