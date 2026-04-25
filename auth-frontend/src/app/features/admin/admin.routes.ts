import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout/admin-layout';

export const adminRoutes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
      {
        path: 'usuarios',
        loadComponent: () => import('./usuarios/admin-usuarios').then(m => m.AdminUsuariosComponent)
      },
      {
        path: 'aplicacoes',
        loadComponent: () => import('./aplicacoes/admin-aplicacoes').then(m => m.AdminAplicacoesComponent)
      }
    ]
  }
];
