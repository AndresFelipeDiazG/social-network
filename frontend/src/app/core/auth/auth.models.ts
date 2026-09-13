export interface AuthenticatedUser {
  readonly id: string;
  readonly username: string;
  readonly displayName: string;
}

export interface LoginResponse {
  readonly accessToken: string;
  readonly tokenType: string;
  readonly expiresIn: number;
  readonly user: AuthenticatedUser;
}

export interface Credentials {
  readonly username: string;
  readonly password: string;
}
