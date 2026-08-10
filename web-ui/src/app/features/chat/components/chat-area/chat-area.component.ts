import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { AvatarModule } from 'primeng/avatar';
import { TextareaModule } from 'primeng/textarea';
import { MarkdownModule } from 'ngx-markdown';
import { ChatMessage } from '../../../../core/services/chat-websocket.service';

@Component({
  selector: 'app-chat-area',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, InputTextModule, AvatarModule, TextareaModule, MarkdownModule],
  templateUrl: './chat-area.component.html'
})
export class ChatAreaComponent {
  @Input() messages: ChatMessage[] = [];
  @Input() isWaitingForResponse = false;
  @Input() userInput = '';
  @Input() agentMode = 'GENERAL';
  
  @Output() userInputChange = new EventEmitter<string>();
  @Output() onSendMessage = new EventEmitter<void>();
  @Output() openSettings = new EventEmitter<void>();

  send() {
    this.onSendMessage.emit();
  }

  onKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      if (this.userInput.trim()) {
        this.send();
      }
    }
  }
}
