import { Injectable } from '@angular/core';
import { MessageService, ConfirmationService } from 'primeng/api';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  constructor(
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  success(summary: string, detail: string = ''): void {
    this.messageService.add({ severity: 'success', summary, detail, life: 3000 });
  }

  error(summary: string, detail: string = ''): void {
    this.messageService.add({ severity: 'error', summary, detail, life: 5000 });
  }

  info(summary: string, detail: string = ''): void {
    this.messageService.add({ severity: 'info', summary, detail, life: 3000 });
  }

  warn(summary: string, detail: string = ''): void {
    this.messageService.add({ severity: 'warn', summary, detail, life: 4000 });
  }

  confirm(options: { 
    message: string; 
    header?: string; 
    icon?: string; 
    acceptLabel?: string;
    rejectLabel?: string;
    acceptButtonStyleClass?: string;
    rejectButtonStyleClass?: string;
    accept: () => void; 
    reject?: () => void; 
  }): void {
    this.confirmationService.confirm({
      message: options.message,
      header: options.header || '確認操作',
      icon: options.icon || 'pi pi-exclamation-triangle',
      acceptLabel: options.acceptLabel || '確認',
      rejectLabel: options.rejectLabel || '取消',
      acceptButtonStyleClass: options.acceptButtonStyleClass || 'p-button-danger',
      rejectButtonStyleClass: options.rejectButtonStyleClass || 'p-button-text p-button-secondary',
      accept: options.accept,
      reject: options.reject
    });
  }
}
