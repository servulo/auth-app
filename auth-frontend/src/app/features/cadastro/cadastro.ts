import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-cadastro',
  imports: [
    ReactiveFormsModule, RouterLink,
    CardModule, InputTextModule, PasswordModule, ButtonModule, MessageModule
  ],
  providers: [MessageService],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.scss'
})
export class CadastroComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);
  private toast  = inject(MessageService);

  loading = signal(false);
  error   = signal('');

  form = this.fb.group({
    firstName: ['', Validators.required],
    lastName:  ['', Validators.required],
    email:     ['', [Validators.required, Validators.email]],
    username:  ['', Validators.required],
    password:  ['', [Validators.required, Validators.minLength(6)]]
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    this.error.set('');

    this.auth.register(this.form.getRawValue() as any).subscribe({
      next: () => {
        this.toast.add({ severity: 'success', summary: 'Conta criada!', detail: 'Faça login para continuar.' });
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.message ?? 'Não foi possível criar a conta.';
        this.error.set(msg);
      }
    });
  }
}
