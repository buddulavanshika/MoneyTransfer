export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  accountId: number;
  holderName: string;
}

export interface AuthUser {
  accountId: number;
  holderName: string;
  username: string;
}