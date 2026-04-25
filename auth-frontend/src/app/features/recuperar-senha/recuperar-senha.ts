import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-recuperar-senha',
  imports: [
    ReactiveFormsModule, RouterLink,
    CardModule, InputTextModule, ButtonModule, MessageModule
  ],
  templateUrl: './recuperar-senha.html',
  styleUrl: './recuperar-senha.scss'
})
export class RecuperarSenhaComponent {
  private fb   = inject(FormBuilder);
  private auth = inject(AuthService);

  loading = signal(false);
  sent    = signal(false);
  error   = signal('');

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    this.error.set('');

    this.auth.forgotPassword({ email: this.form.value.email! }).subscribe({
      next: () => {
        this.loading.set(false);
        this.sent.set(true);
      },
      error: () => {
        this.loading.set(false);
        // Não expõe se o e-mail existe ou não (segurança)
        this.sent.set(true);
      }
    });
  }
}
