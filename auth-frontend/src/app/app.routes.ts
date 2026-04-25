import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'cadastro',
    loadComponent: () => import('./features/cadastro/cadastro').then(m => m.CadastroComponent)
  },
  {
    path: 'recuperar-senha',
    loadComponent: () => import('./features/recuperar-senha/recuperar-senha').then(m => m.RecuperarSenhaComponent)
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./features/perfil/perfil').then(m => m.PerfilComponent)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.adminRoutes)
  },
  { path: '**', redirectTo: 'login' }
];
