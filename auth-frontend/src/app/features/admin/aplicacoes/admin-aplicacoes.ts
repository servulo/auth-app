import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AdminService } from '../../../core/services/admin.service';
import { Application } from '../../../core/models/admin.models';

@Component({
  selector: 'app-admin-aplicacoes',
  imports: [
    ReactiveFormsModule,
    TableModule, ButtonModule, TagModule, DialogModule,
    InputTextModule, ConfirmDialogModule, TooltipModule, ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './admin-aplicacoes.html'
})
export class AdminAplicacoesComponent implements OnInit {
  private adminService = inject(AdminService);
  private confirm      = inject(ConfirmationService);
  private toast        = inject(MessageService);
  private fb           = inject(FormBuilder);

  apps      = signal<Application[]>([]);
  loading   = signal(true);
  showModal = signal(false);
  saving    = signal(false);

  form = this.fb.group({
    clientId:    ['', Validators.required],
    name:        ['', Validators.required],
    description: ['']
  });

  ngOnInit(): void { this.loadApps(); }

  loadApps(): void {
    this.loading.set(true);
    this.adminService.listApplications().subscribe({
      next:  (a) => { this.apps.set(a); this.loading.set(false); },
      error: ()  => this.loading.set(false)
    });
  }

  openCreateModal(): void {
    this.form.reset();
    this.showModal.set(true);
  }

  create(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.adminService.createApplication(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.saving.set(false);
        this.showModal.set(false);
        this.toast.add({ severity: 'success', summary: 'Criado', detail: 'Aplicação criada.' });
        this.loadApps();
      },
      error: () => {
        this.saving.set(false);
        this.toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível criar.' });
      }
    });
  }

  delete(app: Application): void {
    this.confirm.confirm({
      message: `Deseja remover a aplicação "${app.name}"?`,
      header: 'Confirmar exclusão',
      icon: 'pi pi-trash',
      accept: () => {
        this.adminService.deleteApplication(app.id).subscribe({
          next: () => {
            this.toast.add({ severity: 'success', summary: 'Removido', detail: 'Aplicação removida.' });
            this.loadApps();
          },
          error: () => this.toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível remover.' })
        });
      }
    });
  }
}
