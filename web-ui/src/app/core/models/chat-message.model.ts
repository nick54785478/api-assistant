export interface ChatMessage {
  role: 'user' | 'ai';
  content: string;
  isDelta?: boolean;
  isFinal?: boolean;
  agentMode?: string;
}
