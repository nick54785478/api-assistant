export interface PlaybookStep {
  name: string;
  description: string;
  requiredTool?: string;
  responseInstructions?: string;
  customInputs?: StepParameter[];
}

export interface StepParameter {
  type: string;
  key: string;
  value: string;
}

export interface Playbook {
  id: string;
  agentSessionId?: string;
  name: string;
  description: string;
  steps: PlaybookStep[];
  createdAt: string;
  updatedAt: string;
}

export interface CreatePlaybookRequest {
  name: string;
  description: string;
  steps: PlaybookStep[];
}
