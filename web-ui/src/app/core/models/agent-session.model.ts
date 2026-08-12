export interface AgentSession {
  sessionId: string;
  name: string;
  status: string;
  createdAt: string;
}

export interface PlaybookExecutionLog {
  id: string;
  sessionId: string;
  playbookId: string;
  runId: string | null;
  stepIndex: number;
  status: string;
  errorMessage: string | null;
  detailMessage: string | null;
  createdAt: string;
}

export interface PlaybookRun {
  runId: string;
  playbookId: string;
  status: string;
  totalSteps: number;
  startedAt: string;
  logs: PlaybookExecutionLog[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
