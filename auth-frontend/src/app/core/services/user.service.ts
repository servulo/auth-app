import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserProfile, UserProfileRequest } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  getProfile(keycloakId: string): Observable<UserProfile> {
    return this.http.get<UserProfile>(
      `${environment.apiUrl}/v1/users/${keycloakId}/profile`
    );
  }

  updateProfile(keycloakId: string, request: UserProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(
      `${environment.apiUrl}/v1/users/${keycloakId}/profile`,
      request
    );
  }
}
