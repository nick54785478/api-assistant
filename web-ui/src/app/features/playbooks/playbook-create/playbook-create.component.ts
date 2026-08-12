import { Component, inject, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { PlaybookService } from '../services/playbook.service';
import { AgentSessionService } from '../../../core/services/agent-session.service';
import { AgentSession } from '../../../core/models/agent-session.model';
import { StorageService } from '../../../core/services/storage.service';
import { McpToolService } from '../../../core/services/mcp-tool.service';
import { MessageService } from 'primeng/api';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DropdownModule } from 'primeng/dropdown';

@Component({
  selector: 'app-playbook-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, ButtonModule, InputTextModule, TextareaModule, DropdownModule],
  templateUrl: './playbook-create.component.html',
  styleUrls: ['./playbook-create.component.scss']
})
export class PlaybookCreateComponent implements OnInit {
  @Input() playbookId?: string;
  @Input() agentSessionId?: string;
  
  parameterTypes = [
    { label: 'Request Body', value: 'REQUEST_BODY' },
    { label: 'Request Param', value: 'REQUEST_PARAM' },
    { label: 'Path Variable', value: 'PATH_VARIABLE' },
    { label: 'Header', value: 'HEADER' },
    { label: 'Other', value: 'OTHER' }
  ];
  
  @Output() onSaved = new EventEmitter<void>();
  @Output() onCanceled = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private playbookService = inject(PlaybookService);
  private agentSessionService = inject(AgentSessionService);
  private storageService = inject(StorageService);
  private mcpToolService = inject(McpToolService);
  private messageService = inject(MessageService);

  isEditMode = false;
  
  sessions: any[] = [];
  selectedSessionId: string | null = null;
  availableTools: any[] = [];

  playbookForm: FormGroup = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    steps: this.fb.array([])
  });

  get steps() {
    return this.playbookForm.get('steps') as FormArray;
  }

  getCustomInputs(stepIndex: number): FormArray {
    return this.steps.at(stepIndex).get('customInputs') as FormArray;
  }

  ngOnInit() {
    // Load sessions
    const username = this.storageService.getItem('username') || 'Guest';
    this.agentSessionService.getSessions(username).subscribe({
      next: (sessions: any[]) => this.sessions = sessions,
      error: (err: any) => {
        console.error('Failed to load sessions', err);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load sessions' });
      }
    });

    // Listen to tools
    this.mcpToolService.tools$.subscribe(tools => {
      this.availableTools = [
        { label: '-- None --', value: '' },
        ...tools.map(t => ({ label: t.name, value: t.name }))
      ];
    });
    // Load global tools by default
    this.mcpToolService.refreshTools('global');

    if (this.agentSessionId) {
      this.selectedSessionId = this.agentSessionId;
      this.onSessionSelect(null);
    }

    if (this.playbookId) {
      this.isEditMode = true;
      this.playbookService.getPlaybook(this.playbookId).subscribe({
        next: (playbook) => {
          this.selectedSessionId = playbook.agentSessionId;
          if (this.selectedSessionId) {
            this.onSessionSelect(null);
          }
          this.playbookForm.patchValue({
            name: playbook.name,
            description: playbook.description
          });
          playbook.steps.forEach((step: any) => {
            const stepForm = this.fb.group({
              name: [step.name, Validators.required],
              description: [step.description, Validators.required],
              requiredTool: [step.requiredTool],
              responseInstructions: [step.responseInstructions],
              customInputs: this.fb.array([])
            });
            
            const customInputsArray = stepForm.get('customInputs') as FormArray;
            if (step.customInputs && Array.isArray(step.customInputs)) {
              step.customInputs.forEach((input: any) => {
                customInputsArray.push(this.fb.group({
                  type: [input.type || 'OTHER'],
                  key: [input.key, Validators.required],
                  value: [input.value, Validators.required]
                }));
              });
            }
            
            this.steps.push(stepForm);
          });
        },
        error: (err) => {
          console.error('Error loading playbook', err);
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load playbook' });
        }
      });
    } else {
      // Create mode: add an empty step by default so the user immediately sees where to enter process data
      this.addStep();
    }
  }

  onSessionSelect(event: any) {
    if (this.selectedSessionId) {
      this.mcpToolService.refreshTools('session', this.selectedSessionId);
    } else {
      this.mcpToolService.refreshTools('global');
    }
  }

  addStep() {
    const stepForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      requiredTool: [''],
      responseInstructions: [''],
      customInputs: this.fb.array([])
    });
    this.steps.push(stepForm);
  }

  addCustomInput(stepIndex: number) {
    const customInputsArray = this.getCustomInputs(stepIndex);
    customInputsArray.push(this.fb.group({
      type: ['OTHER'],
      key: ['', Validators.required],
      value: ['', Validators.required]
    }));
  }

  removeCustomInput(stepIndex: number, inputIndex: number) {
    const customInputsArray = this.getCustomInputs(stepIndex);
    customInputsArray.removeAt(inputIndex);
  }

  removeStep(index: number) {
    this.steps.removeAt(index);
  }

  cancel(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.onCanceled.emit();
  }

  onSubmit(event?: Event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    if (this.playbookForm.valid) {
      const payload = {
        agentSessionId: this.selectedSessionId,
        ...this.playbookForm.value
      };

      if (this.isEditMode && this.playbookId) {
        this.playbookService.updatePlaybook(this.playbookId, payload).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Playbook updated successfully' });
            this.onSaved.emit();
          },
          error: (err: any) => {
            console.error('Error updating playbook', err);
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to update playbook' });
          }
        });
      } else {
        this.playbookService.createPlaybook(payload).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Playbook created successfully' });
            this.onSaved.emit();
          },
          error: (err: any) => {
            console.error('Error creating playbook', err);
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to create playbook' });
          }
        });
      }
    } else {
      this.playbookForm.markAllAsTouched();
    }
  }
}
