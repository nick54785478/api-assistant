import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: 'playbooks',
        loadComponent: () => import('./features/playbooks/playbook-list/playbook-list.component').then(m => m.PlaybookListComponent)
    },
    {
        path: 'playbooks/new',
        loadComponent: () => import('./features/playbooks/playbook-create/playbook-create.component').then(m => m.PlaybookCreateComponent)
    },
    {
        path: 'playbooks/edit/:id',
        loadComponent: () => import('./features/playbooks/playbook-create/playbook-create.component').then(m => m.PlaybookCreateComponent)
    }
];
