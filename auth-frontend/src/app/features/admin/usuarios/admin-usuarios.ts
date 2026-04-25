import { Component, inject, signal, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { AdminService } from '../../../core/services/admin.service';
import { AdminUser } from '../../../core/models/admin.models';

@Component({
  selector: 'app-admin-usuarios',
  imports: [
    TableModule, ButtonModule, TagModule,
    SkeletonModule, ConfirmDialogModule, TooltipModule, ToastModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './admin-usuarios.html'
})
export class AdminUsuariosComponent implements OnInit {
  private adminService = inject(AdminService);
  private confirm      = inject(ConfirmationService);
  private toast        = inject(MessageService);

  users   = signal<AdminUser[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.adminService.listUsers().subscribe({
      next:  (u) => { this.users.set(u);  this.loading.set(false); },
      error: ()  => this.loading.set(false)
    });
  }

  toggleStatus(user: AdminUser): void {
    const action = user.enabled ? 'bloquear' : 'desbloquear';
    this.confirm.confirm({
      message: `Deseja ${action} o usuário ${user.username}?`,
      header: 'Confirmar',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.adminService.updateUserStatus(user.id, { enabled: !user.enabled }).subscribe({
          next: () => {
            this.toast.add({ severity: 'success', summary: 'Atualizado', detail: `Usuário ${action}ado.` });
            this.loadUsers();
          },
          error: () => this.toast.add({ severity: 'error', summary: 'Erro', detail: 'Não foi possível atualizar.' })
        });
      }
    });
  }
}
