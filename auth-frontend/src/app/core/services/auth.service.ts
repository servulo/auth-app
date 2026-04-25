import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginRequest, LoginResponse, RegisterRequest,
  ForgotPasswordRequest, TokenPayload
} from '../models/auth.models';

const ACCESS_TOKEN_KEY  = 'auth_access_token';
const REFRESH_TOKEN_KEY = 'auth_refresh_token';
const CLIENT_ID         = 'auth-frontend';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http   = inject(HttpClient);
  private router = inject(Router);

  private _token = signal<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));

  readonly isAuthenticated = computed(() => {
    const token = this._token();
    if (!token) return false;
    try {
      const payload = this.decodeToken(token);
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  });

  readonly currentUser = computed(() => {
    const token = this._token();
    if (!token) return null;
    try { return this.decodeToken(token); } catch { return null; }
  });

  readonly isAdmin = computed(() => {
    const user = this.currentUser();
    return user?.resource_access?.[CLIENT_ID]?.roles?.includes('super-admin') ?? false;
  });

  // ── Token storage ───────────────────────────────────────────────────────────

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  private storeTokens(response: LoginResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY,  response.access_token);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refresh_token);
    this._token.set(response.access_token);
  }

  private clearTokens(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this._token.set(null);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  getKeycloakId(): string | null {
    return this.currentUser()?.sub ?? null;
  }

  // ── JWT decode ──────────────────────────────────────────────────────────────

  private decodeToken(token: string): TokenPayload {
    const payload = token.split('.')[1];
    const base64  = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded  = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=');
    // atob() devolve string binária (Latin-1); TextDecoder reinterpreta como UTF-8
    const bytes   = Uint8Array.from(atob(padded), c => c.charCodeAt(0));
    return JSON.parse(new TextDecoder('utf-8').decode(bytes));
  }

  // ── API calls ───────────────────────────────────────────────────────────────

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/v1/auth/login`, request)
      .pipe(tap(res => this.storeTokens(res)));
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/v1/auth/register`, request);
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http
        .post(`${environment.apiUrl}/v1/auth/logout`, { refreshToken })
        .subscribe({ error: () => {} });
    }
    this.clearTokens();
    this.router.navigate(['/login']);
  }

  refresh(): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/v1/auth/refresh`, {
        refreshToken: this.getRefreshToken()
      })
      .pipe(tap(res => this.storeTokens(res)));
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/v1/auth/forgot-password`, request);
  }
}
