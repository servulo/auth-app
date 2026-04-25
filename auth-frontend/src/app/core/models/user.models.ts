export interface UserProfile {
  id: string;
  keycloakId: string;
  avatarUrl: string | null;
  bio: string | null;
  preferences: Record<string, unknown> | null;
  createdAt: string;
  updatedAt: string;
}

export interface UserProfileRequest {
  avatarUrl?: string;
  bio?: string;
  preferences?: Record<string, unknown>;
}
