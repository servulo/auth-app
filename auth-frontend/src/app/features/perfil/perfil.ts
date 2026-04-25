import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { MessageModule } from 'primeng/message';
import { AvatarModule } from 'primeng/avatar';
import { TagModule } from 'primeng/tag';
import { SkeletonModule } from 'primeng/skeleton';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { UserProfile } from '../../core/models/user.models';

@Component({
  selector: 'app-perfil',
  imports: [
    ReactiveFormsModule,
    CardModule, InputTextModule, TextareaModule,
    ButtonModule, MessageModule, AvatarModule, TagModule, SkeletonModule
  ],
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss'
})
export class PerfilComponent implements OnInit {
  private fb          = inject(FormBuilder);
  private auth        = inject(AuthService);
  private userService = inject(UserService);
  private router      = inject(Router);

  profile  = signal<UserProfile | null>(null);
  loading  = signal(true);
  saving   = signal(false);
  success  = signal(false);
  error    = signal('');

  readonly user     = this.auth.currentUser;
  readonly isAdmin  = this.auth.isAdmin;

  form = this.fb.group({
    avatarUrl:   [''],
    bio:         [''],
  });

  ngOnInit(): void {
    const id = this.auth.getKeycloakId();
    if (!id) { this.router.navigate(['/login']); return; }

    this.userService.getProfile(id).subscribe({
      next: (p) => {
        this.profile.set(p);
        this.form.patchValue({
          avatarUrl: p.avatarUrl ?? '',
          bio:       p.bio ?? ''
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  save(): void {
    const id = this.auth.getKeycloakId();
    if (!id) return;

    this.saving.set(true);
    this.success.set(false);
    this.error.set('');

    this.userService.updateProfile(id, {
      avatarUrl: this.form.value.avatarUrl || undefined,
      bio:       this.form.value.bio       || undefined
    }).subscribe({
      next: (p) => {
        this.profile.set(p);
        this.saving.set(false);
        this.success.set(true);
      },
      error: () => {
        this.saving.set(false);
        this.error.set('Não foi possível salvar o perfil.');
      }
    });
  }

  logout(): void {
    this.auth.logout();
  }
}
