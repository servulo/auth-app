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
  selector: 'app-login',
  imports: [
    ReactiveFormsModule, RouterLink,
    CardModule, InputTextModule, PasswordModule, ButtonModule, MessageModule
  ],
  providers: [MessageService],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  private fb      = inject(FormBuilder);
  private auth    = inject(AuthService);
  private router  = inject(Router);
  private toast   = inject(MessageService);

  loading = signal(false);
  error   = signal('');

  form = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    this.error.set('');

    this.auth.login(this.form.getRawValue() as { username: string; password: string })
      .subscribe({
        next: () => {
          const isAdmin = this.auth.isAdmin();
          this.router.navigate([isAdmin ? '/admin' : '/perfil']);
        },
        error: (err) => {
          this.loading.set(false);
          const msg = err?.error?.error_description ?? 'Usuário ou senha incorretos.';
          this.error.set(msg);
        }
      });
  }
}
