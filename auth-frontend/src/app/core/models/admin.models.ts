export interface AdminUser {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  emailVerified: boolean;
  createdTimestamp: number;
}

export interface AdminUserRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
}

export interface UserStatusRequest {
  enabled: boolean;
}

export interface Application {
  id: string;
  clientId: string;
  name: string;
  description: string;
  enabled: boolean;
}

export interface ApplicationRequest {
  clientId: string;
  name: string;
  description?: string;
}

export interface Role {
  id: string;
  name: string;
  description: string;
}

export interface RoleRequest {
  name: string;
  description?: string;
}

export interface UserRoleRequest {
  roleName: string;
  clientId: string;
}
