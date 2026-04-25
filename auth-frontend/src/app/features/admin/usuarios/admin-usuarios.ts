import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { AdminService } from '../../../core/services/admin.service';
import { AdminUser } from '../../../core/models/admin.models';
import { RegisterRequest } from '../../../core/models/auth.models';

@Component({
  selector: 'app-admin-usuarios',
  imports: [
    TableModule, ButtonModule, TagModule,
    SkeletonModule, ConfirmDialogModule, TooltipModule, ToastModule,
    DialogModule, ReactiveFormsModule, InputTextModule, PasswordModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './admin-usuarios.html'
})
export class AdminUsuariosComponent implements OnInit {
  private adminService = inject(AdminService);
  private confirm      = inject(ConfirmationService);
  private toast        = inject(MessageService);
  private fb           = inject(FormBuilder);

  users         = signal<AdminUser[]>([]);
  loading       = signal(true);
  dialogVisible = signal(false);
  dialogLoading = signal(false);
  dialogError   = signal('');

  form = this.fb.group({
    firstName: ['', Validators.required],
    lastName:  ['', Validators.required],
    email:     ['', [Validators.required, Validators.email]],
    username:  ['', Validators.required],
    password:  ['', [Validators.required, Validators.minLength(6)]]
  });

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

  openDialog(): void {
    this.form.reset();
    this.dialogError.set('');
    this.dialogVisible.set(true);
  }

  createUser(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.dialogLoading.set(true);
    this.dialogError.set('');

    this.adminService.createUser(this.form.getRawValue() as RegisterRequest).subscribe({
      next: () => {
        this.dialogLoading.set(false);
        this.dialogVisible.set(false);
        this.toast.add({ severity: 'success', summary: 'Usuário criado', detail: 'O novo usuário foi cadastrado com sucesso.' });
        this.loadUsers();
      },
      error: (err) => {
        this.dialogLoading.set(false);
        const msg = err?.error?.message ?? 'Não foi possível criar o usuário.';
        this.dialogError.set(msg);
      }
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
