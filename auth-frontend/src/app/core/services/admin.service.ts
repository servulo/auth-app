import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminUser, AdminUserRequest, UserStatusRequest,
  Application, ApplicationRequest,
  Role, RoleRequest, UserRoleRequest
} from '../models/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/v1/admin`;

  // ── Usuários ────────────────────────────────────────────────────────────────
  listUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.base}/users`);
  }

  getUser(id: string): Observable<AdminUser> {
    return this.http.get<AdminUser>(`${this.base}/users/${id}`);
  }

  updateUser(id: string, request: AdminUserRequest): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.base}/users/${id}`, request);
  }

  updateUserStatus(id: string, request: UserStatusRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/users/${id}/status`, request);
  }

  listUserRoles(id: string): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.base}/users/${id}/roles`);
  }

  assignRoleToUser(id: string, request: UserRoleRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/users/${id}/roles`, request);
  }

  removeRoleFromUser(id: string, roleName: string, clientId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/users/${id}/roles/${roleName}?clientId=${clientId}`
    );
  }

  // ── Aplicações ──────────────────────────────────────────────────────────────
  listApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.base}/applications`);
  }

  createApplication(request: ApplicationRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/applications`, request);
  }

  deleteApplication(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/applications/${id}`);
  }

  listApplicationRoles(clientId: string): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.base}/applications/${clientId}/roles`);
  }

  createApplicationRole(clientId: string, request: RoleRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/applications/${clientId}/roles`, request);
  }

  deleteApplicationRole(clientId: string, roleName: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/applications/${clientId}/roles/${roleName}`);
  }
}
