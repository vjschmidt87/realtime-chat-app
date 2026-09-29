export interface User {
  username: string;
  displayName: string;
  email: string;
  status: 'ONLINE' | 'OFFLINE' | 'AWAY';
  avatarUrl?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  displayName: string;
  email: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  displayName: string;
}
