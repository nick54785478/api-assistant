import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { McpToolService } from '../../../../core/services/mcp-tool.service';
import { McpTool, McpToolParameter } from '../../../../core/models/mcp-tool.model';

import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { SelectModule } from 'primeng/select';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-mcp-tool-manager',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InputTextModule,
    TextareaModule,
    SelectModule,
    InputSwitchModule,
    ButtonModule,
    ToastModule
  ],
  templateUrl: './mcp-tool-manager.component.html',
  styleUrls: ['./mcp-tool-manager.component.scss'],
  providers: [MessageService]
})
export class McpToolManagerComponent implements OnInit {
  @Input() mode: 'global' | 'session' = 'global';
  @Input() sessionId: string = '';
  @Output() close = new EventEmitter<void>();

  tools: McpTool[] = [];
  selectedTool: McpTool | null = null;
  isEditing = false;
  isLoading = false;

  methodOptions = [
    { label: 'GET', value: 'GET' },
    { label: 'POST', value: 'POST' },
    { label: 'PUT', value: 'PUT' },
    { label: 'DELETE', value: 'DELETE' }
  ];

  typeOptions = [
    { label: '字串 (String)', value: 'string' },
    { label: '數字 (Number)', value: 'number' },
    { label: '布林 (Boolean)', value: 'boolean' }
  ];

  constructor(
    private toolService: McpToolService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.loadTools();
  }

  loadTools(): void {
    this.isLoading = true;
    this.toolService.refreshTools(this.mode, this.sessionId);
    this.toolService.tools$.subscribe(tools => {
      this.tools = tools;
      this.isLoading = false;
    });
  }

  onClose(): void {
    this.close.emit();
  }

  createNewTool(): void {
    this.selectedTool = {
      name: '',
      description: '',
      api_url: '',
      api_method: 'GET',
      requires_auth: true,
      parameters: []
    };
    this.isEditing = true;
  }

  editTool(tool: McpTool): void {
    // deep copy to avoid modifying original until saved
    this.selectedTool = JSON.parse(JSON.stringify(tool));
    this.isEditing = true;
  }

  addParameter(): void {
    if (this.selectedTool) {
      this.selectedTool.parameters.push({
        name: '',
        type: 'string',
        description: '',
        required: true
      });
    }
  }

  removeParameter(index: number): void {
    if (this.selectedTool) {
      this.selectedTool.parameters.splice(index, 1);
    }
  }

  cancelEdit(): void {
    this.selectedTool = null;
    this.isEditing = false;
  }

  saveTool(): void {
    if (!this.selectedTool?.name || !this.selectedTool?.api_url) {
      this.messageService.add({ severity: 'error', summary: '錯誤', detail: '名稱與 API 網址為必填欄位' });
      return;
    }

    this.isLoading = true;
    if (this.selectedTool.id) {
      this.toolService.updateTool(this.selectedTool).subscribe(() => {
        this.messageService.add({ severity: 'success', summary: '成功', detail: '工具已更新' });
        this.cancelEdit();
        this.isLoading = false;
      });
    } else {
      this.toolService.addTool(this.selectedTool, this.mode).subscribe(() => {
        this.messageService.add({ severity: 'success', summary: '成功', detail: '工具已建立' });
        this.cancelEdit();
        this.isLoading = false;
      });
    }
  }

  deleteTool(id: number | undefined): void {
    if (!id) return;
    if (confirm('確定要刪除這個工具嗎？此操作無法復原。')) {
      this.isLoading = true;
      this.toolService.deleteTool(id).subscribe(() => {
        this.messageService.add({ severity: 'info', summary: '已刪除', detail: '工具已移除' });
        this.isLoading = false;
      });
    }
  }
}
