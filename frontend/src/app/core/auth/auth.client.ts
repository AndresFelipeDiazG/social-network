import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/app-config';
import { AuthenticatedUser, Credentials, LoginResponse } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthClient {

  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(APP_CONFIG).apiBaseUrl;

  /**
   * GET con la cabecera Authorization: Basic. Es un GET por contrato, y las
   * credenciales van en la cabecera y no en la query para que no queden en los
   * logs del servidor ni en el historial del navegador.
   */
  login(credentials: Credentials): Observable<LoginResponse> {
    return this.http.get<LoginResponse>(`${this.baseUrl}/auth/login`, {
      headers: new HttpHeaders({ Authorization: `Basic ${encodeBasic(credentials)}` }),
    });
  }

  me(): Observable<AuthenticatedUser> {
    return this.http.get<AuthenticatedUser>(`${this.baseUrl}/auth/me`);
  }
}

/**
 * btoa solo acepta latin1 y lanza con cualquier caracter fuera de ese rango, de
 * modo que una contrasena con acentos romperia el login. Se codifica a UTF-8
 * primero, que es como el backend decodifica la cabecera.
 */
function encodeBasic({ username, password }: Credentials): string {
  const utf8 = new TextEncoder().encode(`${username}:${password}`);
  return btoa(String.fromCharCode(...utf8));
}
